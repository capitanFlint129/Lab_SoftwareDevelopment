package ru.byprogminer.shellin

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class StateTest {

    @Test
    fun testWorkByDefault() {
        val state = State(mutableMapOf())

        assertTrue { state.work }
    }

    @Test
    fun testPreserveEnvironment() {
        val environment = mutableMapOf("a" to "b", "c" to "d")
        val state = State(environment.toMutableMap())

        assertEquals(environment, state.environment)
    }

    @Test
    fun testStopOnce() {
        val state = State(mutableMapOf())

        state.stop()
        assertFalse { state.work }
    }

    @Test
    fun testStopTwice() {
        val state = State(mutableMapOf())

        state.stop()

        assertThrows<IllegalStateException> { state.stop() }
        assertFalse { state.work }
    }

    @Test
    fun testPwdGet() {
        val state = State(mutableMapOf(State.PWD_VARIABLE to "test 123"))

        assertEquals("test 123", state.environment[State.PWD_VARIABLE])
        assertEquals("test 123", state.pwd.toString())
    }

    @Test
    fun testPwdSetDirectory() {
        val tmpDir = Files.createTempDirectory("testPwdSet")
        val tmpDirParent = tmpDir.parent

        val state = State(mutableMapOf(State.PWD_VARIABLE to tmpDirParent.toString()))

        assertEquals(tmpDirParent.toString(), state.environment[State.PWD_VARIABLE])
        assertEquals(tmpDirParent, state.pwd)

        state.pwd = tmpDir

        assertEquals(tmpDir.toString(), state.environment[State.PWD_VARIABLE])
        assertEquals(tmpDir, state.pwd)
    }

    @Test
    fun testPwdSetRegularFile() {
        val tmpDir = Files.createTempFile("testPwdSet", "file")
        val tmpDirParent = tmpDir.parent

        val state = State(mutableMapOf(State.PWD_VARIABLE to tmpDirParent.toString()))

        assertEquals(tmpDirParent.toString(), state.environment[State.PWD_VARIABLE])
        assertEquals(tmpDirParent, state.pwd)

        assertThrows<IllegalArgumentException> {
            state.pwd = tmpDir
        }

        assertEquals(tmpDirParent.toString(), state.environment[State.PWD_VARIABLE])
        assertEquals(tmpDirParent, state.pwd)
    }

    @Test
    fun testPath() {
        val path = listOf("a", "b", "c", "d", "e")
        val pathStr = path.joinToString(File.pathSeparator)
        val state = State(mutableMapOf(State.PATH_VARIABLE to pathStr))

        assertEquals(pathStr, state.environment[State.PATH_VARIABLE])
        assertEquals(path.map(Paths::get), state.path)

        state.environment[State.PATH_VARIABLE] = "123"

        assertEquals("123", state.environment[State.PATH_VARIABLE])
        assertEquals(listOf(Paths.get("123")), state.path)
    }

    @Test
    fun testCopyAll() {
        val state1 = State(mutableMapOf("123" to "345"))
        state1.stop()

        val state2 = state1.copy()

        assertFalse(state2.work)
        assertEquals(mapOf("123" to "345"), state2.environment)
    }

    @Test
    fun testCopyIsolated1() {
        val state1 = State(mutableMapOf("123" to "345"))
        val state2 = state1.copy()
        state2.stop()

        assertTrue(state1.work)
        assertFalse(state2.work)

        state2.environment["123"] = "321"

        assertEquals(mapOf("123" to "345"), state1.environment)
        assertEquals(mapOf("123" to "321"), state2.environment)
    }

    @Test
    fun testCopyIsolated2() {
        val state1 = State(mutableMapOf("123" to "345"))
        val state2 = state1.copy()
        state1.stop()

        assertFalse(state1.work)
        assertTrue(state2.work)

        state1.environment["123"] = "321"

        assertEquals(mapOf("123" to "321"), state1.environment)
        assertEquals(mapOf("123" to "345"), state2.environment)
    }
}
