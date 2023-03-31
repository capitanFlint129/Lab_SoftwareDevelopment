package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.BufferedInputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream
import java.util.*
import kotlin.concurrent.thread


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

    override fun exec(input: BufferedInputStream, output: PrintStream, error: PrintStream, state: State) {
        val inputs = List(commands.size - 1) { PipedInputStream() }
        val outputs = (0 until commands.size - 1).map(inputs::get).map(::PipedOutputStream)
        val exceptions = arrayOfNulls<Throwable?>(commands.size)

        val threads = commands.mapIndexed { idx, cmd ->
            return@mapIndexed thread {
                val cmdInput = if (idx == 0) input else BufferedInputStream(inputs[idx - 1])
                val cmdOutput = if (idx == commands.size - 1) output else PrintStream(outputs[idx])

                try {
                    cmd.exec(cmdInput, cmdOutput, error, state.copy())
                } catch (e: Throwable) {
                    exceptions[idx] = e
                } finally {
                    if (idx > 0) {
                        cmdInput.close()
                    }

                    if (idx < commands.size - 1) {
                        cmdOutput.close()
                    }
                }
            }
        }

        threads.forEach(Thread::join)

        if (exceptions.any(Objects::nonNull)) {
            throw exceptions.filterNotNull().reduce { acc, e ->
                acc.addSuppressed(e)
                return@reduce acc
            }
        }
    }
}
