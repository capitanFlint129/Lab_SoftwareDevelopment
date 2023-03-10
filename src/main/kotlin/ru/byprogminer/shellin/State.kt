package ru.byprogminer.shellin


class State(
    /**
     * Environment variables map.
     */
    val environment: MutableMap<String, String>,
) {

    /**
     * Is state working (not stopped yet).
     * Default is `true`.
     */
    var work = true
        private set

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
