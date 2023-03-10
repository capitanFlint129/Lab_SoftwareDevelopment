package ru.byprogminer.shellin

import ru.byprogminer.shellin.command.Command
import java.io.BufferedInputStream
import java.io.OutputStream


class Runner(
    private val state: State,
) {

    /**
     * Executes command.
     *
     * TODO it could be better if we handle special control characters like ^D and ^C to control
     *      work of the command, e.g. create isolated input stream for each command
     */
    fun exec(command: Command, input: BufferedInputStream, output: OutputStream, error: OutputStream) {
        command.exec(input, output, error, state)
    }
}
