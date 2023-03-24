package ru.byprogminer.shellin.command

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.default
import ru.byprogminer.shellin.State
import java.io.BufferedInputStream
import java.io.OutputStreamWriter
import java.io.PrintStream


class GrepCommand(
    private val args: List<String>,
) : Command {

    override fun exec(input: BufferedInputStream, output: PrintStream, error: PrintStream, state: State) {
        val args = try {
            ArgParser(args.subList(1, args.size).toTypedArray()).parseInto(::GrepArgs)
        } catch (e: SystemExitException) {
            val writer = OutputStreamWriter(if (e.returnCode == 0) output else error)
            e.printUserMessage(writer, "grep", 0)
            writer.flush()
            return
        }

        output.println("${args.word} ${args.caseInsensitive} ${args.linesAfter} ${args.pattern} ${args.file}")
    }

    private class GrepArgs(parser: ArgParser) {

        val word by parser.flagging("-w",
            help = "search only words")

        val caseInsensitive by parser.flagging("-i",
            help = "search case-insensitive")

        val linesAfter by parser.storing("-A",
            help = "specifies how much lines need to print after each match",
            transform = String::toInt)
            .default(0)

        val pattern by parser.positional("PATTERN", "pattern to search for")

        val file by parser.positional("FILE", "file to search in")
            .default(null)
    }
}
