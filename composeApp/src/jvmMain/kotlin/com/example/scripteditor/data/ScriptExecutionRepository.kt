package com.example.scripteditor.data

import com.example.scripteditor.core.ExecutionEvent
import kotlinx.coroutines.flow.Flow

interface ScriptExecutionRepository {
    fun run(command: String, arguments: List<String>): Flow<ExecutionEvent>
}