package com.example.scripteditor.core.repositories

import com.example.scripteditor.core.repositories.ExecutionEvent.Finished
import com.example.scripteditor.core.repositories.ExecutionEvent.StdErr
import com.example.scripteditor.core.repositories.ExecutionEvent.StdOut
import com.example.scripteditor.core.repositories.ExecutionEvent.SystemError

sealed class ExecutionEvent(var index: Int = 0) {
    data class StdOut(val line: String) : ExecutionEvent()
    data class StdErr(val line: String) : ExecutionEvent()
    data class Finished(val exitCode: Int) : ExecutionEvent()
    data class SystemError(val message: String) : ExecutionEvent()
}

fun ExecutionEvent.getLine() = when (this) {
    is Finished -> "FINISHED. Code: $exitCode"
    is StdErr -> line
    is StdOut -> line
    is SystemError -> "ERR: $message"
}