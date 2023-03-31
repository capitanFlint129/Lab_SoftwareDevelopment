package ru.byprogminer.shellin.util

import java.io.BufferedInputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels


const val READ_LINE_BUFFER_SIZE = 1024

/**
 * Reads line from input stream with preserving trailing bytes.
 *
 * If EOF reached returns `null`.
 * End of line is LF. Encoding is UTF-8.
 * EOL is preserved in the end of returned string.
 *
 * @return read line or `null` if EOF
 */
fun BufferedInputStream.readLine(): String? {
    require(this.markSupported())

    val channel = Channels.newChannel(this)
    var buffer = ByteBuffer.allocate(READ_LINE_BUFFER_SIZE)

    while (true) {
        val pos = buffer.position()

        // assert invariant
        require(buffer.hasRemaining())

        // mark current position to reset to when line will read
        this.mark(buffer.remaining())

        // read up to buffer remaining
        val wasRead = channel.read(buffer)

        // if EOF - there isn't any line, return null
        // this isn't exactly -1 to prevent infinite loop
        if (wasRead <= 0) {
            return null
        }

        // try to find LF in read bytes

        var ok = false
        for (i in pos until buffer.position()) {
            if (buffer[i] == '\n'.code.toByte()) {
                // if found - break the loop and save position

                ok = true
                buffer.flip()
                buffer.position(i + 1)
                break
            }
        }

        // if LF found reset input to mark and skip bytes to EOL
        if (ok) {
            this.reset()
            this.skip((wasRead - buffer.remaining()).toLong())
            break
        }

        // if we reached end of buffer, reallocate with multiplication strategy
        // to get amortized complexity O(n)
        if (!buffer.hasRemaining()) {
            buffer.flip()

            val newBuffer = ByteBuffer.allocate(buffer.capacity() * 2)
            newBuffer.put(buffer)
            buffer = newBuffer
        }
    }

    // decode read line with UTF-8

    buffer.flip()
    return Charsets.UTF_8.decode(buffer).toString()
}
