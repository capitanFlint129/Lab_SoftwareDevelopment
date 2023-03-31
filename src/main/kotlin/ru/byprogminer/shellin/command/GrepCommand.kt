package ru.byprogminer.shellin.command

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.default
import ru.byprogminer.shellin.State
import ru.byprogminer.shellin.util.readLine
import java.io.BufferedInputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.io.PrintStream
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.NoSuchFileException


/**
 * Grep command filters input text, or text from file, if specified, with Java regular expression pattern.
 *
 * If -A specified and positive, each match following with specified amount of lines without duplicating
 * like as in default grep.
 */
class GrepCommand(
    private val argsList: List<String>,
) : Command {

    private lateinit var args: GrepArgs
    private lateinit var regex: Regex

    override fun exec(input: BufferedInputStream, output: PrintStream, error: PrintStream, state: State) {
        args = try {
            ArgParser(argsList.subList(1, argsList.size).toTypedArray()).parseInto(::GrepArgs)
        } catch (e: SystemExitException) {
            val writer = OutputStreamWriter(if (e.returnCode == 0) output else error)
            e.printUserMessage(writer, "grep", 0)
            writer.flush()
            return
        }

        initRegex()

        if (args.file != null) {
            val path = state.pwd.resolve(args.file!!)

            try {
                BufferedInputStream(Files.newInputStream(path)).use { stream ->
                    grep(stream, output)
                }
            } catch (e: NoSuchFileException) {
                error.println("File not found: \"$path\".")
                return
            } catch (e: AccessDeniedException) {
                error.println("Access denied: \"$path\".")
                return
            } catch (e: IOException) {
                error.println("Cannot read file: \"${e.localizedMessage}\".")
                return
            }
        } else {
            grep(input, output)
        }
    }

    private fun initRegex() {
        var pattern = args.pattern

        if (args.word) {
            pattern = "\\b$pattern\\b"
        }

        val options = mutableSetOf<RegexOption>()

        if (args.caseInsensitive) {
            options.add(RegexOption.IGNORE_CASE)
        }

        regex = Regex(pattern, options)
    }

    private fun grep(input: BufferedInputStream, output: PrintStream) {
        var line = input.readLine()

        var linesAfter = 0

        while (line != null) {
            if (regex.containsMatchIn(line)) {
                output.print(line)
                linesAfter = args.linesAfter
            } else if (linesAfter > 0) {
                output.print(line)
                --linesAfter
            }

            line = input.readLine()
        }
    }

    private class GrepArgs(parser: ArgParser) {

        val word by parser.flagging("-w",
            help = "search only words")

        val caseInsensitive by parser.flagging("-i",
            help = "search case-insensitive")

        val linesAfter by parser.storing("-A",
            help = "specifies how much lines need to print after each match") {
            try {
                return@storing toInt()
            } catch (e: IllegalArgumentException) {
                throw SystemExitException("-A value must be an integer", 3)
            }
        }
            .default(0)
            .addValidator {
                if (value < 0) {
                    throw SystemExitException("-A value must be non-negative", 3)
                }
            }

        val pattern by parser.positional("PATTERN",
            help = "pattern to search for")

        val file by parser.positional("FILE",
            help = "file to search in")
            .default(null)
    }
}
