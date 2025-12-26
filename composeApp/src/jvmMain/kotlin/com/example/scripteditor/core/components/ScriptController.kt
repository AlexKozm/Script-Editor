package com.example.scripteditor.core.components

import com.example.scripteditor.core.ExecutionState
import com.example.scripteditor.core.repositories.ExecutionEvent
import com.example.scripteditor.core.repositories.ScriptExecution
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ScriptController {
    val scriptOutput: Flow<IndexedValue<ExecutionEvent>>
    val executionState: StateFlow<ExecutionState>
    fun saveScript(path: String, script: String)
    fun saveAndRun(path: String, script: String, scriptExecution: ScriptExecution)
    fun stop()
}