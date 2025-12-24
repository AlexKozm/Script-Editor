package com.example.scripteditor

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeAppDesktopTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun runScript(): Unit = runBlocking {
        ScriptRunner().saveAndRun($$"""
            val a = 1
            println("hello world $a")
        """.trimIndent()).collect { println(it) }

//        ScriptRunner().executeCommand("ls").collect { println(it) }

//        val process = ProcessBuilder("ls")
//            .redirectErrorStream(true)
//            .start()
//        process.waitFor()
//        process.inputStream.bufferedReader().forEachLine {println(it)}
    }
}