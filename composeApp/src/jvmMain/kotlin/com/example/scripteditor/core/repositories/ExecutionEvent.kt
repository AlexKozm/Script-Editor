package com.example.scripteditor.core.repositories

sealed class ExecutionEvent {
    data class StdOut(val line: String) : ExecutionEvent()
    data class StdErr(val line: String) : ExecutionEvent()
    data class Finished(val exitCode: Int) : ExecutionEvent()
    data class SystemError(val message: String) : ExecutionEvent()
}