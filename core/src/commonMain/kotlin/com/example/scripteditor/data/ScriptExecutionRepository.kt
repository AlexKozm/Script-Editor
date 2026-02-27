package com.example.scripteditor.data

import com.example.scripteditor.core.models.ExecutionEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow

interface ScriptExecutionRepository {
    fun run(command: String, arguments: List<String>): Flow<ExecutionEvent>
}

interface ScriptExecutionStoppableRepository: ScriptExecutionRepository {
    fun run(command: String, arguments: List<String>, stopSignal: CompletableDeferred<Unit>): Flow<ExecutionEvent>

    override fun run(command: String, arguments: List<String>): Flow<ExecutionEvent> =
        run(command, arguments, CompletableDeferred())
}

expect fun ScriptExecutionRepository(): ScriptExecutionRepository
expect fun ScriptExecutionSequentialRepository(): ScriptExecutionStoppableRepository