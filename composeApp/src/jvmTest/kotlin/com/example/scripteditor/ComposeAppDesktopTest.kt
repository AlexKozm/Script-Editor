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
    fun splitTest() {
        val text = """
            import asas
            fun a() {
                var b = 0
            }
            fun c() {
                val b = 1
            }
            
        """.trimIndent()
        val res = Regex("fun\\b").findAll(text).toList().map { it.range }
        println(res)
    }
}