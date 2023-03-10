package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.BufferedInputStream
import java.io.PrintStream


interface Command {

    /**
     * Executes command.
     *
     * @param input standard input stream
     * @param output standard output stream
     * @param error standard error stream
     * @param state current state
     */
    fun exec(input: BufferedInputStream, output: PrintStream, error: PrintStream, state: State)
}
