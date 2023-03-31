package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import kotlin.test.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.io.path.pathString

class CdCommandTest {

    private val input = ByteArrayInputStream("".toByteArray())
    private val output = ByteArrayOutputStream()
    private val error = ByteArrayOutputStream()
    private val state = State(System.getenv().toMutableMap())
    private val homePath = System.getProperty("user.home")

    @Test
    fun `test with no arguments`() {
        CdCommand(listOf("")).exec(input.buffered(), PrintStream(output), PrintStream(error), state)

        assertEquals(homePath, state.pwd.toString())
    }

    @Test
    fun `test with valid argument`() {
        val newPath = Files.createTempDirectory("testDir")
        CdCommand(listOf("", newPath.pathString)).exec(
            input.buffered(),
            PrintStream(output),
            PrintStream(error),
            state
        )

        assertEquals(newPath.toString(), state.pwd.toString())
        newPath.toFile().deleteRecursively()
    }

    @Test
    fun `test with invalid argument`() {
        CdCommand(listOf("", "invalidPath")).exec(input.buffered(), PrintStream(output), PrintStream(error), state)

        assertTrue(error.toString().contains("cd: invalidPath: No such file or directory"))
    }

    @Test
    fun `test with too many arguments`() {
        CdCommand(listOf("", "arg1", "arg2")).exec(input.buffered(), PrintStream(output), PrintStream(error), state)

        assertTrue(error.toString().contains("Too many arguments"))
    }

    @Test
    fun `test with dot argument`() {
        state.pwd = state.pwd.resolve(homePath)
        val initialPath = state.pwd
        CdCommand(listOf("", ".")).exec(input.buffered(), PrintStream(output), PrintStream(error), state)

        assertEquals(initialPath.toString(), state.pwd.toString())
    }

    @Test
    fun `test with double dot argument`() {
        state.pwd = state.pwd.resolve(homePath)
        val parentPath = state.pwd.parent ?: error("Cannot get parent directory of ${state.pwd}")
        CdCommand(listOf("", "..")).exec(input.buffered(), PrintStream(output), PrintStream(error), state)

        assertEquals(parentPath.toString(), state.pwd.toString())
    }
}
