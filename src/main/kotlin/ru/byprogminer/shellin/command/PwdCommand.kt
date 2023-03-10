package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream


/**
 * Prints current directory path.
 */
class PwdCommand(
    private val args: List<String>,
) : Command {

    private companion object {

        const val PWD_VARIABLE = "PWD"
    }

    override fun exec(input: InputStream, output: OutputStream, error: OutputStream, state: State) {
        if (args.size > 1) {
            PrintStream(error).println("Too many arguments.")
            return
        }

        val pwd = state.environment.getOrDefault(PWD_VARIABLE, "")
        PrintStream(output).println(pwd)
    }
}
