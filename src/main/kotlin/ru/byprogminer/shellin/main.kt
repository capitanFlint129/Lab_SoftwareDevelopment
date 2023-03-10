package ru.byprogminer.shellin

import java.io.BufferedInputStream


private const val PROMPT = "> "

fun main() {
    val state = State(System.getenv().toMutableMap())
    val parser = Parser(state)
    val runner = Runner(state)

    val input = BufferedInputStream(System.`in`)
    val output = System.out

    while (state.work) {
        output.print(PROMPT)
        output.flush()

        try {
            val command = parser.parse(input)
            runner.exec(command, input, output)
        } catch (e: Exception) {
            TODO()
        }
    }
}
