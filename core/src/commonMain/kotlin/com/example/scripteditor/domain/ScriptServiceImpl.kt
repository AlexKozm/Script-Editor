package com.example.scripteditor.domain

import com.example.scripteditor.core.ExecutionEvent
import com.example.scripteditor.core.ExecutionState
import com.example.scripteditor.data.ScriptExecutionRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ScriptServiceImpl(
    private val scriptExecutionRepository: ScriptExecutionRepository = ScriptExecutionRepository(),
) : ScriptService {
    private val _executionState: MutableStateFlow<ExecutionState> = MutableStateFlow(ExecutionState.STOPPED)
    override val executionState: StateFlow<ExecutionState> = _executionState

    private val executionRequestFlow: MutableSharedFlow<ScriptParams?> = MutableSharedFlow()

    override val scriptOutput: Flow<ExecutionEvent> = flow {
        executionRequestFlow.collectLatest { request ->
            _executionState.value = request?.let { ExecutionState.RUNNING } ?: ExecutionState.STOPPED
            request?.run { scriptExecutionRepository.run(command, arguments).collect(::emit) }
        }
    }

    override suspend fun runScript(command: String, arguments: List<String>) =
        executionRequestFlow.emit(ScriptParams(command, arguments))

    override suspend fun stopScript() {
        _executionState.value = ExecutionState.STOPPING
        executionRequestFlow.emit(null)
    }
}

private data class ScriptParams(
    val command: String,
    val arguments: List<String>
)