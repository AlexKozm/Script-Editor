package com.example.scripteditor.domain

import com.example.scripteditor.core.models.ExecutionEvent
import com.example.scripteditor.core.models.ExecutionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow


interface ScriptStateHolder {
    /**
     * Flow is empty until [runScript] is called.
     */
    val scriptOutput: SharedFlow<ExecutionEvent>
    val executionState: StateFlow<ExecutionState>

    fun runScript(command: String, arguments: List<String>)
    fun stopScript()
}