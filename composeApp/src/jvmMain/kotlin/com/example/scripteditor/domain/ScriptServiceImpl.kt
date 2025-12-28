package com.example.scripteditor.domain

import com.example.scripteditor.core.ExecutionEvent
import com.example.scripteditor.core.ExecutionState
import com.example.scripteditor.data.ScriptExecutionRepository
import com.example.scripteditor.data.ScriptExecutionRepositoryImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ScriptServiceImpl(
    private val scriptExecutionRepository: ScriptExecutionRepository = ScriptExecutionRepositoryImpl(),
) : ScriptService {

    private val _scriptOutput: MutableSharedFlow<ExecutionEvent> = MutableSharedFlow()
    override val scriptOutput: SharedFlow<ExecutionEvent> = _scriptOutput.asSharedFlow()

    private val _executionState: MutableStateFlow<ExecutionState> = MutableStateFlow(ExecutionState.STOPPED)
    override val executionState: StateFlow<ExecutionState> = _executionState

    private var executionJob: Job? = null

    override suspend fun runScript(command: String, arguments: List<String>): Unit = coroutineScope {
        stopScript()
        executionJob = launch(Dispatchers.Default) {
            _executionState.value = ExecutionState.RUNNING
            scriptExecutionRepository.run(command, arguments).collect(_scriptOutput::emit)
        }
        executionJob?.invokeOnCompletion { _executionState.value = ExecutionState.STOPPED }
    }

    override suspend fun stopScript() = coroutineScope {
        _executionState.value = ExecutionState.STOPPING
        executionJob?.cancelAndJoin()
        executionJob = null
    }
}