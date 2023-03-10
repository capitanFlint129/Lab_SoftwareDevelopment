package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
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

    override fun exec(input: InputStream, output: OutputStream, error: OutputStream, state: State) {
        if (args.size > 2) {
            PrintStream(error).println("Too many arguments.")
            return
        }

        if (args.size == 1) {
            val home = System.getProperty("user.home")

            PrintStream(output).println(home)
            state.pwd = Paths.get(home)
            return
        }

        try {
            state.pwd = state.pwd.resolve(args[1])
        } catch (e: IllegalArgumentException) {
            PrintStream(error).println("\"${args[1]}\" is not a directory.")
        }
    }
}
