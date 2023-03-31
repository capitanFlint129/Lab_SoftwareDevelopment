package ru.byprogminer.shellin.command

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import ru.byprogminer.shellin.State
import java.io.BufferedInputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class AssignCommandTest {

    @Test
    fun testAssignGlobal() {
        val input = mockk<BufferedInputStream>()
        val output = mockk<PrintStream>()
        val error = mockk<PrintStream>()

        val state = State(mutableMapOf())
        val command = AssignCommand(
            variable = "test_var",
            value = "test value",
            command = null
        )

        command.exec(input, output, error, state)

        assertTrue(state.work)
        assertEquals(mapOf("test_var" to "test value"), state.environment)
    }

    @Test
    fun testAssignLocal() {
        val input = mockk<BufferedInputStream>()
        val output = mockk<PrintStream>()
        val error = mockk<PrintStream>()

        val innerCommand = mockk<Command>()
        val states = mutableListOf<State>()

        every { innerCommand.exec(input, output, error, capture(states)) } returns Unit

        val state = State(mutableMapOf())
        val command = AssignCommand(
            variable = "test_var",
            value = "test value",
            command = innerCommand
        )

        command.exec(input, output, error, state)

        verify(exactly = 1) { innerCommand.exec(input, output, error, any()) }

        assertEquals(1, states.size)

        assertTrue(state.work)
        assertEquals(mapOf(), state.environment)

        val innerState = states[0]
        assertTrue(innerState.work)
        assertEquals(mapOf("test_var" to "test value"), innerState.environment)

        confirmVerified(innerCommand)
    }
}
