package ru.byprogminer.shellin

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class State(
    /**
     * Environment variables map.
     */
    val environment: MutableMap<String, String>,
) {

    companion object {

        /**
         * Name of current working directory environment variable.
         */
        const val PWD_VARIABLE = "PWD"
    }

    /**
     * Is state working (not stopped yet).
     * Default is `true`.
     */
    var work = true
        private set

    /**
     * Helper accessor to PWD environment variable.
     */
    var pwd: Path
        get() = Paths.get(environment.getOrDefault(PWD_VARIABLE, ""))
        set(value) {
            require(Files.isDirectory(value)) { "PWD must be directory" }
            environment[PWD_VARIABLE] = value.toAbsolutePath().normalize().toString()
        }

    /**
     * Set [work] to false
     *
     * @throws IllegalStateException if already stopped
     */
    fun stop() {
        if (!work) {
            throw IllegalStateException("cannot stop stopped state")
        }

        work = false
    }

    /**
     * Copies state
     *
     * @return copy of state
     */
    fun copy(): State {
        val result = State(environment.toMutableMap())
        result.work = work
        return result
    }
}
