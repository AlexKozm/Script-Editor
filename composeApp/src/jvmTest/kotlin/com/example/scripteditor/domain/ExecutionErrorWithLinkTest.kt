package com.example.scripteditor.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

private data class Data(
    val name: String,
    val script: String,
    val errorText: String,
    // as script from the beginning till cursor position
    val expectedScriptUntilCursor: String?
)

class ParseExecutionErrorForScriptRefTest : FunSpec({

    val data = listOf(
        Data(
            name = "3:1",
            script = """
                        |val a = 1
                        |fun b() = 2
                        |error
                        |class C {}
                        """.trimMargin(),
            errorText = """
                        |some-file.kts:3:1: some error
                        """.trimMargin(),
            expectedScriptUntilCursor = """
                        |val a = 1
                        |fun b() = 2
                        |e""".trimMargin(),
        ),
        Data(
            name = "last char",
            script = """
                        |val a = 1
                        |fun b() = 2
                        |e""".trimMargin(),
            errorText = """
                        |some-file.kts:3:1: some error
                        """.trimMargin(),
            expectedScriptUntilCursor = """
                        |val a = 1
                        |fun b() = 2
                        |e""".trimMargin(),
        ),
        Data(
            name = "unexisted char on existed line without more lines",
            script = """
                        |val a = 1
                        |fun b() = 2
                        |fun e""".trimMargin(),
            errorText = """
                        |some-file.kts:3:6: some error
                        """.trimMargin(),
            expectedScriptUntilCursor = """
                        |val a = 1
                        |fun b() = 2
                        |fun e""".trimMargin(),
        ),
        Data(
            name = "unexisted char on existed line with more lines (should put cursor before \\n)",
            script = """
                        |val a = 1
                        |fun b() = 2
                        |fun e
                        |fun d() = 3""".trimMargin(),
            errorText = """
                        |some-file.kts:3:6: some error
                        """.trimMargin(),
            expectedScriptUntilCursor = """
                        |val a = 1
                        |fun b() = 2
                        |fun e""".trimMargin(),
        ),
        Data(
            name = "unexisted line",
            script = """
                        |val a = 1
                        |fun b() = 2
                        |fun e""".trimMargin(),
            errorText = """
                        |some-file.kts:4:6: some error
                        """.trimMargin(),
            expectedScriptUntilCursor = null
        )

    )

    withData(
        nameFn = { it.name },
        ts = data
    ) { (_, script, errorText, expectedScriptUntilCursor) ->
        val scriptUntilCursor = parseExecutionErrorForScriptRef(errorText)
            ?.getCursorPlace(script)
            ?.let { script.slice(0..it) }
        scriptUntilCursor shouldBe expectedScriptUntilCursor
    }

})