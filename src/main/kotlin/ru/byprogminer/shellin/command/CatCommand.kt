package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.NoSuchFileException


/**
 * Cat command prints file specified by argument to the output stream.
 */
class CatCommand(
    private val args: List<String>,
) : Command {

    init {
        require(args.isNotEmpty())
    }

    override fun exec(input: InputStream, output: OutputStream, error: OutputStream, state: State) {
        if (args.size != 2) {
            PrintStream(output).println("Usage: ${args[0]} <FILE>")
            return
        }

        val path = state.pwd.resolve(args[1])

        try {
            Files.newInputStream(path).use {
                it.copyTo(output)
            }
        } catch (e: NoSuchFileException) {
            PrintStream(error).println("File not found: \"$path\".")
        } catch (e: AccessDeniedException) {
            PrintStream(error).println("Access denied: \"$path\".")
        } catch (e: IOException) {
            PrintStream(error).println("Cannot read file: \"${e.localizedMessage}\".")
        }
    }
}
