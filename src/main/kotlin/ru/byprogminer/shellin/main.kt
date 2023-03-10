package ru.byprogminer.shellin

import java.io.BufferedInputStream
import java.io.EOFException


private const val PROMPT = "> "

fun main() {
    val state = State(System.getenv().toMutableMap())
    val parser = Parser(state)
    val runner = Runner(state)

    val input = BufferedInputStream(System.`in`)
    val output = System.out
    val error = System.err

    while (state.work) {
        output.print(PROMPT)
        output.flush()

        val command = try {
            parser.parse(input) ?: continue
        } catch (e: EOFException) {
            state.stop()
            continue
        }

        try {
            runner.exec(command, input, output, error)
        } catch (e: Exception) {
            TODO()
        }
    }

    output.println("exit")
}
