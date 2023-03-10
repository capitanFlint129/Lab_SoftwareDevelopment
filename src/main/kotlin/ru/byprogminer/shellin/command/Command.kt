package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.InputStream
import java.io.OutputStream


interface Command {

    /**
     * Executes command.
     *
     * @param input standard input stream
     * @param output standard output stream
     * @param error standard error stream
     * @param state current state
     */
    fun exec(input: InputStream, output: OutputStream, error: OutputStream, state: State)
}
