package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.BufferedInputStream
import java.io.IOException
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

    private companion object {

        const val BUFFER_SIZE = 4096
    }

    init {
        require(args.isNotEmpty())
    }

    override fun exec(input: BufferedInputStream, output: OutputStream, error: OutputStream, state: State) {
        val cmdArray = args.toTypedArray()

        // we must do that because JVM don't provide access to run apps from specified PATH...
        cmdArray[0] = resolveExecutable(state).absolute().normalize().toString()

        val process = Runtime.getRuntime().exec(
            cmdArray,
            state.environment.map { (k, v) -> "$k=$v" }.toTypedArray(),
            state.pwd.toFile(),
        )

        // all of these threads will ends with process automatically

        thread {
            try {
                process.outputStream.use {
                    transferToProcess(input, it)
                }
            } catch (e: IOException) {
                // ignored
            }
        }

        val outputThread = thread {
            try {
                process.inputStream.use {
                    it.copyTo(output)
                }
            } catch (e: IOException) {
                // ignored
            }
        }

        val errorThread = thread {
            try {
                process.errorStream.use {
                    it.copyTo(error)
                }
            } catch (e: IOException) {
                // ignored
            }
        }

        process.waitFor()
        outputThread.join()
        errorThread.join()

        // we mustn't join thread for input stream because it will stop only when
        // user print something after process finished
    }

    /**
     * Transfers our input stream to another process with prior to
     * preserve bytes after process exit.
     */
    private fun transferToProcess(self: BufferedInputStream, other: OutputStream) {
        require(self.markSupported())

        val buffer = ByteArray(BUFFER_SIZE)

        // we must synchronize on self because otherwise there could be a deadlock
        // when another thread will would read from it
        synchronized(self) {
            while (true) {
                self.mark(BUFFER_SIZE)

                val wasRead = self.read(buffer)
                if (wasRead <= 0) {
                    break
                }

                var wasWrote = 0

                // OutputStream.write(ByteArray) doesn't provide information
                // about amount of written bytes, so we need to write them one by one
                while (wasWrote < wasRead) {
                    try {
                        other.write(buffer[wasWrote].toInt())
                        other.flush()
                    } catch (e: IOException) {
                        break
                    }

                    ++wasWrote
                }

                // if an exception occurred, restore trailing bytes and stop
                if (wasWrote < wasRead) {
                    self.reset()
                    self.skip(wasWrote.toLong())
                    break
                }
            }
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
