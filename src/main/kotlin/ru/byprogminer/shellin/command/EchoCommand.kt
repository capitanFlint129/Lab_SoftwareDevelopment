package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.BufferedInputStream
import java.io.PrintStream


/**
 * Prints arguments joined with space.
 */
class EchoCommand(
    private val args: List<String>,
) : Command {

    init {
        require(args.isNotEmpty())
    }

    override fun exec(input: BufferedInputStream, output: PrintStream, error: PrintStream, state: State) {
        val result = args.subList(1, args.size).joinToString(" ")
        output.println(result)
    }
}
