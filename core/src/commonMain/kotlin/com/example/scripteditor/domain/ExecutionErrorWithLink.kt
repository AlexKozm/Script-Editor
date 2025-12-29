package com.example.scripteditor.domain

/**
 * Example:
 *  some-file.kts:3:412: error: something terrible happened
 *  |             | |  | |                                |
 *  |             |col | +-+-----------------------------+
 *  |          line    |   |
 *  +------------------+   err
 *  |
 *  link
 */
data class ExecutionErrorWithLink(
    val link: String,
    val line: Int,
    val col: Int,
    val err: String,
)
private val regex = Regex(""":(\d+):(\d+):""")

fun parseExecutionErrorForScriptRef(errLine: String): ExecutionErrorWithLink? {
    val res = regex.find(errLine) ?: return null

    val line = res.groups[1]?.value?.toIntOrNull() ?: return null
    val col = res.groups[2]?.value?.toIntOrNull() ?: return null

    val group0Range = res.groups[0]?.range ?: return null
    val file = group0Range.first.let { errLine.slice(0 until it) }

    val err = group0Range.last.let { errLine.slice(it until errLine.length) }
    val link = "$file:$line:$col"
    return ExecutionErrorWithLink(link, line, col, err)
}

fun ExecutionErrorWithLink.getCursorPlace(
    script: String
): Int? {
    val place = script
        .split("\n")
        .take(line)
        .mapIndexed { index, string -> if (index != line - 1) string.length else col - 1 }
        .sum() + line - 1

    return if (line > script.count { it == '\n' } + 1) null
    else if (script.getOrNull(place) == '\n') place - 1
    else if (place !in script.indices) script.lastIndex
    else place
}