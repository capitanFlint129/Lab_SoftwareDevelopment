package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.InputStream
import java.io.OutputStream


/**
 * Pipeline command executes commands and connects input and output streams.
 * Error streams will be merged in error stream of pipeline.
 */
class PipelineCommand(
    private val commands: List<Command>,
) : Command {

    override fun exec(input: InputStream, output: OutputStream, error: OutputStream, state: State) {
        TODO("Not yet implemented")
    }
}
