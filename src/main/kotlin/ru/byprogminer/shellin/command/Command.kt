package ru.byprogminer.shellin.command

import java.io.InputStream
import java.io.OutputStream


interface Command {

    fun exec(input: InputStream, output: OutputStream, error: OutputStream)
}
