package ru.byprogminer.shellin.command

import ru.byprogminer.shellin.State
import java.io.BufferedInputStream
import java.io.OutputStream


/**
 * Assign command executes specified another command with the modified state
 * or, if command is not presented, modifies current state.
 */
class AssignCommand(
    private val variable: String,
    private val value: String,
    private val command: Command?,
) : Command {

    override fun exec(input: BufferedInputStream, output: OutputStream, error: OutputStream, state: State) {
        if (command == null) {
            state.environment[variable] = value
            return
        }

        val newState = state.copy()
        newState.environment[variable] = value
        command.exec(input, output, error, newState)
    }
}
