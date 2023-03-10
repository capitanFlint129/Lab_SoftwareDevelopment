package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths


/**
 * Change current directory to argument.
 *
 * If no argument specified, changes to home directory.
 * If argument specify not a directory path, prints error.
 */
class CdCommand(
    private val args: List<String>,
) : Command {

    private companion object {

        const val PWD_VARIABLE = "PWD"
    }

    override fun exec(input: InputStream, output: OutputStream, error: OutputStream, state: State) {
        if (args.size > 2) {
            PrintStream(error).println("Too many arguments.")
            return
        }

        if (args.size == 1) {
            val home = System.getProperty("user.home")

            PrintStream(output).println(home)
            state.environment[PWD_VARIABLE] = home
            return
        }

        val pwd = state.environment.getOrDefault(PWD_VARIABLE, "")
        val newPath = Paths.get(pwd).resolve(args[1])

        if (!Files.isDirectory(newPath)) {
            PrintStream(error).println("$newPath is not a directory.")
            return
        }

        state.environment[PWD_VARIABLE] = newPath.toAbsolutePath().normalize().toString()
    }
}
