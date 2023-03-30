package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.BufferedInputStream
import java.io.IOException
import java.io.PrintStream
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.NoSuchFileException


/**
 * Ls - list directory contents
 */
class LsCommand(
    private val args: List<String>,
) : Command {

    init {
        require(args.isNotEmpty())
    }

    override fun exec(input: BufferedInputStream, output: PrintStream, error: PrintStream, state: State) {
        if (args.size > 2) {
            error.println("Too many arguments")
            return
        }

        val path = if (args.size == 2) state.pwd.resolve(args[1]) else state.pwd

        try {
            Files.walk(path, 1).forEach { output.println(it.fileName) }
        } catch (e: NoSuchFileException) {
            error.println("File not found: \"$path\".")
        } catch (e: AccessDeniedException) {
            error.println("Access denied: \"$path\".")
        } catch (e: IOException) {
            error.println("Cannot read file: \"${e.localizedMessage}\".")
        }
    }
}