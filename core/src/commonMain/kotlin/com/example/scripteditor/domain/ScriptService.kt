package com.example.scripteditor.domain

import com.example.scripteditor.core.ExecutionEvent
import com.example.scripteditor.core.ExecutionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


interface ScriptService {
    /**
     * Flow is empty until [runScript] is called.
     * Script cancels as soon as collector's coroutine scope cancels or [stopScript] is called.
     */
    val scriptOutput: Flow<ExecutionEvent>

    val executionState: StateFlow<ExecutionState>

    /**
     * Works only if [scriptOutput] is collected. Overwise does nothing
     */
    suspend fun runScript(command: String, arguments: List<String>)

    /**
     * Works only if [scriptOutput] is collected. Overwise does nothing
     */
    suspend fun stopScript()
}