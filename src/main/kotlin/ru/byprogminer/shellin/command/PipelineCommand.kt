package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.BufferedInputStream
import java.io.OutputStream


/**
 * Pipeline command executes commands and connects input and output streams.
 * Error streams will be merged in error stream of pipeline.
 */
class PipelineCommand(
    private val commands: List<Command>,
) : Command {

    init {
        require(commands.size > 1)
    }

    override fun exec(input: BufferedInputStream, output: OutputStream, error: OutputStream, state: State) {
        TODO("Not yet implemented")
    }
}
