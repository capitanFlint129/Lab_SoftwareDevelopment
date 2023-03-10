package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.InputStream
import java.io.InterruptedIOException
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.concurrent.thread
import kotlin.io.path.absolute


/**
 * System command attempts to execute command from environment variable `PATH`.
 */
class SystemCommand(
    private val args: List<String>,
) : Command {

    init {
        require(args.isNotEmpty())
    }

    override fun exec(input: InputStream, output: OutputStream, error: OutputStream, state: State) {
        val cmdArray = args.toTypedArray()

        // we must do that because JVM don't provide access to run apps from specified PATH...
        cmdArray[0] = resolveExecutable(state).absolute().normalize().toString()

        val process = Runtime.getRuntime().exec(
            cmdArray,
            state.environment.map { (k, v) -> "$k=$v" }.toTypedArray(),
            state.pwd.toFile(),
        )

        val inputThread = thread {
            try {
                input.copyTo(process.outputStream)
            } catch (e: InterruptedIOException) {
                // ignore
            }
        }

        val outputThread = thread {
            try {
                process.inputStream.copyTo(output)
            } catch (e: InterruptedIOException) {
                // ignore
            }
        }

        val errorThread = thread {
            try {
                process.errorStream.copyTo(error)
            } catch (e: InterruptedIOException) {
                // ignore
            }
        }

        try {
            process.waitFor()
        } finally {
            // TODO async IO will be better solution but it requires change all architecture =)

            Thread.yield()

            inputThread.stop()
            outputThread.stop()
            errorThread.stop()

            inputThread.join()
            outputThread.join()
            errorThread.join()
        }
    }

    /**
     * Tries to find executable from first argument.
     * If it is a complex path, returns resolved from current working directory immediately (without checks).
     * Otherwise, tries to find executable file in the locations specified by PATH environment variable.
     */
    private fun resolveExecutable(state: State): Path {
        val name = Paths.get(args[0])

        if (name.nameCount > 1) {
            return state.pwd.resolve(name)
        }

        for (path in state.path) {
            val filePath = path.resolve(name)

            if (Files.isExecutable(filePath)) {
                return filePath.absolute().normalize()
            }
        }

        throw IllegalArgumentException("No such executable in PATH: \"$name\"")
    }
}
