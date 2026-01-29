package com.example.scripteditor.domain

import com.example.scripteditor.core.ExecutionEvent
import com.example.scripteditor.core.ExecutionState
import com.example.scripteditor.data.ScriptExecutionRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ScriptStateHolderImpl(
    private val scriptExecutionRepository: ScriptExecutionRepository = ScriptExecutionRepository(),
    private val scope: CoroutineScope
) : ScriptStateHolder {
    private val _executionState: MutableStateFlow<ExecutionState> = MutableStateFlow(ExecutionState.STOPPED)
    override val executionState: StateFlow<ExecutionState> = _executionState

    private val executionRequestFlow: MutableSharedFlow<ExecutionRequest> = MutableSharedFlow()

    override val scriptOutput: SharedFlow<ExecutionEvent> = channelFlow {
        executionRequestFlow.collectLatest { request ->
            when (request) {
                is ExecutionRequest.RunScript -> {
                    _executionState.value = ExecutionState.RUNNING
                    with(request) {
                        scriptExecutionRepository.run(command, args).collect(::send)
                    }
                    _executionState.value = ExecutionState.STOPPED
                }
                ExecutionRequest.StopScript -> {
                    _executionState.value = ExecutionState.STOPPED
                }
            }
        }
    }.shareIn(scope = scope, replay = 30000, started = SharingStarted.Eagerly)



    override fun runScript(command: String, arguments: List<String>) {
        scope.launch {
            executionRequestFlow.emit(ExecutionRequest.RunScript(command, arguments))
        }
    }

    override fun stopScript() {
        scope.launch {
            _executionState.value = ExecutionState.STOPPING
            executionRequestFlow.emit(ExecutionRequest.StopScript)
        }
    }
}

private sealed class ExecutionRequest {
    data class RunScript(val command: String, val args: List<String>) : ExecutionRequest()
    object StopScript : ExecutionRequest()
}
