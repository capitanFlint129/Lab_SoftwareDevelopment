package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.BufferedInputStream
import java.io.IOException
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

    override fun exec(input: BufferedInputStream, output: PrintStream, error: PrintStream, state: State) {
        if (args.size != 2) {
            output.println("Usage: ${args[0]} <FILE>")
            return
        }

        val path = state.pwd.resolve(args[1])

        try {
            Files.newInputStream(path).use {
                it.copyTo(output)
            }
        } catch (e: NoSuchFileException) {
            error.println("File not found: \"$path\".")
        } catch (e: AccessDeniedException) {
            error.println("Access denied: \"$path\".")
        } catch (e: IOException) {
            error.println("Cannot read file: \"${e.localizedMessage}\".")
        }
    }
}
