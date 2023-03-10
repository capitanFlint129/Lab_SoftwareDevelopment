package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.InputStream
import java.io.OutputStream


class WcCommand(
    private val args: List<String>,
) : Command {

    override fun exec(input: InputStream, output: OutputStream, error: OutputStream, state: State) {
        TODO("not implemented")
    }
}
