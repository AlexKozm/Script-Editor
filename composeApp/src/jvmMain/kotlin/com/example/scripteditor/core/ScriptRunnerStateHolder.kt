package com.example.scripteditor.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


interface ScriptRunnerStateHolder {
    val command: String
    val arguments: List<String>
    val scriptRunningState: Flow<ExecutionState>
    val scriptOutput: Flow<String>
    val scriptErrors: Flow<String>

    suspend fun run()
    suspend fun cancel()
}