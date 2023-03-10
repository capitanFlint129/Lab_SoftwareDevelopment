package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.NoSuchFileException


class WcCommand(
    private val args: List<String>,
) : Command {

    private companion object {

        const val BUFFER_SIZE = 4096
    }

    override fun exec(input: InputStream, output: OutputStream, error: OutputStream, state: State) {
        if (args.size != 2) {
            PrintStream(output).println("Usage: ${args[0]} <FILE>")
            return
        }

        val path = state.pwd.resolve(args[1])

        try {
            Files.newInputStream(path).use {
                val (lines, words, bytes) = wc(it)

                PrintStream(output).println("$lines\t$words\t$bytes\t${args[1]}")
            }
        } catch (e: NoSuchFileException) {
            PrintStream(error).println("File not found: \"$path\".")
        } catch (e: AccessDeniedException) {
            PrintStream(error).println("Access denied: \"$path\".")
        } catch (e: IOException) {
            PrintStream(error).println("Cannot read file: \"${e.localizedMessage}\".")
        }
    }

    private fun wc(stream: InputStream): Triple<Int, Int, Int> {
        var lines = 0
        var words = 0
        var bytes = 0

        val buffer = ByteArray(BUFFER_SIZE)
        while (true) {
            val wasRead = stream.read(buffer)

            if (wasRead <= 0) {
                break
            }

            bytes += wasRead
            var space = true
            for (i in 0 until wasRead) {
                val c = buffer[i].toInt().toChar()

                if (c == '\n') {
                    ++lines
                }

                if (space && c.isWhitespace()) {
                    continue
                }

                space = c.isWhitespace()
                if (space) {
                    ++words
                }
            }
        }

        return Triple(lines, words, bytes)
    }
}
