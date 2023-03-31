package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.BufferedInputStream
import java.io.PrintStream


/**
 * Prints current directory path.
 */
class PwdCommand(
    private val args: List<String>,
) : Command {

    init {
        require(args.isNotEmpty())
    }

    override fun exec(input: BufferedInputStream, output: PrintStream, error: PrintStream, state: State) {
        if (args.size > 1) {
            error.println("Too many arguments.")
            return
        }

        output.println(state.pwd)
    }
}
