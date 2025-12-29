package com.example.scripteditor.domain

import com.example.scripteditor.core.ExecutionEvent
import com.example.scripteditor.core.ExecutionState

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow


interface ScriptService {
    val scriptOutput: SharedFlow<ExecutionEvent>
    val executionState: StateFlow<ExecutionState>

    suspend fun runScript(command: String, arguments: List<String>)
    suspend fun stopScript()
}