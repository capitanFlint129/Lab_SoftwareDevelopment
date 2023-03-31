package ru.byprogminer.shellin.command

import io.mockk.mockk
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import ru.byprogminer.shellin.State
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals


class LsCommandTest {

    @JvmField
    @Rule
    var folder = TemporaryFolder()

    @Test
    fun testExec() {
        val input = mockk<BufferedInputStream>()
        val outputStream = ByteArrayOutputStream()
        val output = PrintStream(outputStream)
        val error = mockk<PrintStream>()

        folder.create()
        folder.newFile("myfile.txt")
        folder.newFolder("subfolder")


        val state = State(mutableMapOf())
        val command = LsCommand(
            listOf("ls", folder.root.absolutePath)
        )

        command.exec(input, output, error, state)

        assertTrue(state.work)
        assertEquals("myfile.txt\r\nsubfolder\r\n", outputStream.toByteArray().decodeToString())
    }

    @Test
    fun testFileNotFound() {
        val input = mockk<BufferedInputStream>()
        val output = mockk<PrintStream>()
        val errorStream = ByteArrayOutputStream()
        val error = PrintStream(errorStream)


        val state = State(mutableMapOf())
        val command = LsCommand(
            listOf("ls", "this_directory_doesnt_exist")
        )

        command.exec(input, output, error, state)

        assertTrue(state.work)
        assertEquals("File not found: \"this_directory_doesnt_exist\".\r\n", errorStream.toByteArray().decodeToString())
    }
}
