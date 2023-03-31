package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.BufferedInputStream
import java.io.PrintStream
import java.lang.IllegalArgumentException
import java.nio.file.Paths

class CdCommand(
    private val args: List<String>,
) : Command {

    init {
        require(args.isNotEmpty())
    }
    override fun exec(input: BufferedInputStream, output: PrintStream, error: PrintStream, state: State) {
        if (args.size > 2) {
            error.println("Too many arguments")
            return
        }

        if (args.size == 1) {
            state.pwd = Paths.get(System.getProperty("user.home"))
            return
        }
        try {
            state.pwd = state.pwd.resolve(args[1])
        } catch (e: IllegalArgumentException) {
            error.println("cd: ${args[1]}: No such file or directory")
        }
    }

}