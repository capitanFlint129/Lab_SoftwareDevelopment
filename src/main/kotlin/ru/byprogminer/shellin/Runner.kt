package ru.byprogminer.shellin

import ru.byprogminer.shellin.command.Command
import java.io.BufferedInputStream
import java.io.OutputStream


class Runner(
    private val state: State,
) {

    fun exec(command: Command, input: BufferedInputStream, output: OutputStream): Unit = TODO()
}
