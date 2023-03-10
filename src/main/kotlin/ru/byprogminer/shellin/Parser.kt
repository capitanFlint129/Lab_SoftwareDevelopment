package ru.byprogminer.shellin

import ru.byprogminer.shellin.command.Command
import java.io.BufferedInputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels


class Parser(
    private val state: State,
) {

    companion object {

        const val READ_LINE_BUFFER_SIZE = 1024
    }

    fun parse(input: BufferedInputStream): Command {
        val line = readLine(input)

        println(line)
        TODO()
    }

    private fun readLine(input: BufferedInputStream): String? {
        require(input.markSupported())

        val channel = Channels.newChannel(input)
        var buffer = ByteBuffer.allocate(READ_LINE_BUFFER_SIZE)

        while (true) {
            val pos = buffer.position()

            input.mark(buffer.remaining())
            val wasRead = channel.read(buffer)
            if (wasRead == 0) {
                return null
            }

            var ok = false
            for (i in pos until buffer.position()) {
                if (buffer[i] == '\n'.code.toByte()) {
                    ok = true

                    buffer.flip()
                    buffer.position(i + 1)
                    break
                }
            }

            if (ok) {
                input.reset()
                input.skip((wasRead - buffer.remaining()).toLong())
                break
            }

            if (!buffer.hasRemaining()) {
                buffer.flip()

                val newBuffer = ByteBuffer.allocate(buffer.capacity() * 2)
                newBuffer.put(buffer)
                buffer = newBuffer
            }
        }

        buffer.flip()
        return Charsets.UTF_8.decode(buffer).toString()
    }
}
