package com.example.scripteditor.data

import com.example.scripteditor.core.models.ExecutionEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow

interface ScriptExecutionRepository {
    fun run(command: String, arguments: List<String>, stopSignal: CompletableDeferred<Unit>): Flow<ExecutionEvent>
}

expect fun ScriptExecutionRepository(): ScriptExecutionRepository
expect fun ScriptExecutionSequentialRepository(): ScriptExecutionRepository