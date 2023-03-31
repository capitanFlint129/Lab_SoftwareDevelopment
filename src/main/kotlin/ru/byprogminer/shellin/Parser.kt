package ru.byprogminer.shellin

import ru.byprogminer.shellin.command.*
import ru.byprogminer.shellin.util.readLine
import java.io.BufferedInputStream
import java.io.EOFException


/**
 * Parses commands from the input stream and substitute variables from environment.
 */
class Parser(
    private val state: State,
) {

    private companion object {

        val VARIABLE_REGEX = "\\\$([a-zA-Z_]\\w*)".toRegex()
        val ASSIGNMENT_REGEX = "([a-zA-Z_]\\w*)=(.*)".toRegex()

        val INTERNAL_COMMANDS = mapOf(
            "cat" to ::CatCommand,
            "echo" to ::EchoCommand,
            "wc" to ::WcCommand,
            "pwd" to ::PwdCommand,
            "exit" to ::ExitCommand,
            "grep" to ::GrepCommand,
            "ls" to ::LsCommand,
            "cd" to ::CdCommand
        )
    }

    /**
     * Parse command from the first line of input.
     *
     * If EOF reached before line end, throws [java.io.EOFException].
     * If no command was in the first line (e.g. empty line) returns `null`.
     *
     * @param input input stream to read from
     * @return command or `null` if no command presented
     * @throws EOFException if no line in input
     * @throws IllegalArgumentException if first line is malformed command
     */
    fun parse(input: BufferedInputStream): Command? {
        val line = input.readLine() ?: throw EOFException()

        val chunks = prepareLine(line)
        require(chunks.isNotEmpty())

        val commands = chunks.map(this::buildCommand)
        if (commands.size == 1) {
            return commands[0]
        }

        val pipelineCommands = try {
            commands.requireNoNulls()
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("empty command in pipeline")
        }

        return PipelineCommand(pipelineCommands)
    }

    /**
     * Builds [Command] object from the list of arguments.
     *
     * If the list of arguments is empty, returns `null`.
     * Otherwise, the first argument is the name of command.
     *
     * - if the name according to [ASSIGNMENT_REGEX], it is treated as [AssignCommand]
     * - if the name specified in [INTERNAL_COMMANDS], it is treated as according internal command
     * - otherwise, it is treated as [SystemCommand]
     *
     * @param args arguments list
     * @return command or `null` if arguments list is empty
     */
    private fun buildCommand(args: List<String>): Command? {
        if (args.isEmpty()) {
            return null
        }

        val name = args[0]

        val assignmentMatch = ASSIGNMENT_REGEX.matchEntire(name)
        if (assignmentMatch != null) {
            return AssignCommand(
                variable = assignmentMatch.groups[1]!!.value,
                value = assignmentMatch.groups[2]!!.value,
                command = buildCommand(args.subList(1, args.size)),
            )
        }

        val internalCommand = INTERNAL_COMMANDS[name]
        if (internalCommand != null) {
            return internalCommand(args)
        }

        return SystemCommand(args)
    }

    /**
     * Prepare line according to the algorithm described in the architecture notes.
     *
     * Returned pipeline always has at least one command.
     * Commands in pipeline could be empty lists.
     *
     * @return list of pipeline commands, that is list of command name and arguments
     * @throws IllegalArgumentException if line is malformed
     */
    private fun prepareLine(line: String): List<List<String>> {
        val chunks = mutableListOf(mutableListOf<String>())
        val currentChunk = StringBuilder()

        var quote: Char? = null
        var quotesWasInChunk = false
        var quotePos = 0
        var space = true

        for (c in line) {
            if (quote != null) {
                if (c == quote) {
                    if (quote == '"') {
                        substituteVariables(currentChunk, quotePos)
                    }

                    quote = null
                    quotePos = currentChunk.length
                    continue
                }

                currentChunk.append(c)
                continue
            }

            if (space && c.isWhitespace()) {
                continue
            }

            space = c.isWhitespace()
            if (space) {
                substituteVariables(currentChunk, quotePos)
                if (quotesWasInChunk || currentChunk.isNotBlank()) {
                    chunks.last().add(currentChunk.toString())
                }

                quotesWasInChunk = false
                currentChunk.clear()
                quotePos = 0
                continue
            }

            if (c == '\'' || c == '"') {
                substituteVariables(currentChunk, quotePos)

                quote = c
                quotesWasInChunk = true
                quotePos = currentChunk.length
                continue
            }

            if (c == '|') {
                substituteVariables(currentChunk, quotePos)
                if (quotesWasInChunk || currentChunk.isNotBlank()) {
                    chunks.last().add(currentChunk.toString())
                }

                chunks.add(mutableListOf())
                quotesWasInChunk = false
                currentChunk.clear()
                quotePos = 0
                space = true
                continue
            }

            currentChunk.append(c)
        }

        if (quotesWasInChunk) {
            throw IllegalArgumentException("unmatched quote $quote")
        }

        // we assume that line has EOL at the end
        // so that all chunks was already added to the list
        require(currentChunk.isBlank())
        return chunks
    }

    /**
     * Helper method to substitute variables in the suffix of [StringBuilder].
     * See [substituteVariables].
     */
    private fun substituteVariables(chunk: StringBuilder, quotePos: Int) {
        chunk.replace(quotePos, chunk.length, substituteVariables(chunk.substring(quotePos)))
    }

    /**
     * Substitutes variables in string.
     *
     * Variable is substring that according to a [VARIABLE_REGEX].
     * Variables are replacing with values from [state] or empty string if there isn't suitable variable.
     *
     * @param str string to substitute variables in
     * @return substitution result
     */
    private fun substituteVariables(str: String) = VARIABLE_REGEX.replace(str) { match ->
        state.environment.getOrDefault(match.groups[1]!!.value, "")
    }
}
