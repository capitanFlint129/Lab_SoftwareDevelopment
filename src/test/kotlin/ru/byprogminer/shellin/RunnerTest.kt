package ru.byprogminer.shellin

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import ru.byprogminer.shellin.command.Command
import java.io.BufferedInputStream
import java.io.PrintStream


class RunnerTest {

    @Test
    fun testExec() {
        val state = mockk<State>()
        val input = mockk<BufferedInputStream>()
        val output = mockk<PrintStream>()
        val error = mockk<PrintStream>()

        val command = mockk<Command>()
        val runner = Runner(state)

        every { command.exec(input, output, error, state) } returns Unit

        runner.exec(command, input, output, error)

        verify(exactly = 1) { command.exec(input, output, error, state) }
        confirmVerified(command)
    }
}
