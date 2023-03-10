package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.InputStream
import java.io.OutputStream


/**
 * System command attempts to execute command from environment variable `PATH`.
 */
class SystemCommand(
    private val args: List<String>,
) : Command {

    override fun exec(input: InputStream, output: OutputStream, error: OutputStream, state: State) {
        TODO("Not yet implemented")
    }
}
