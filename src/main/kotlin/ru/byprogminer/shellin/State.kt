package ru.byprogminer.shellin


class State(
    val environment: MutableMap<String, String>,
) {

    var work = true
        private set

    fun stop() {
        work = false
    }
}
