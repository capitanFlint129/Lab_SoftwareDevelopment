package ru.byprogminer.shellin

import java.io.BufferedInputStream
import java.io.IOException


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
        } catch (e: IllegalArgumentException) {
            error.println("Malformed command: ${e.localizedMessage}")
            continue
        } catch (e: IOException) {
            state.stop()
            continue
        }

        try {
            runner.exec(command, input, output, error)
        } catch (e: IllegalArgumentException) {
            error.println(e.localizedMessage)
        } catch (e: IOException) {
            error.println("I/O error occurred:\n${e.localizedMessage}")
        } catch (e: Exception) {
            error.println("Unexpected error:\n$e")
        }
    }

    output.println("exit")
}
