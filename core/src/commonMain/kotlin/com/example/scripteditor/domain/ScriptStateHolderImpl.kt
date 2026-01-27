package com.example.scripteditor.domain

import com.example.scripteditor.core.ExecutionEvent
import com.example.scripteditor.core.ExecutionState
import com.example.scripteditor.data.ScriptExecutionRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlin.time.Duration

class ScriptStateHolderImpl(
    private val scriptExecutionRepository: ScriptExecutionRepository = ScriptExecutionRepository(),
    scope: CoroutineScope
) : ScriptStateHolder {
    private val _executionState: MutableStateFlow<ExecutionState> = MutableStateFlow(ExecutionState.STOPPED)
    override val executionState: StateFlow<ExecutionState> = _executionState

    private val executionRequestFlow: MutableSharedFlow<ExecutionRequest> = MutableSharedFlow()
//    private val numOfScriptOutputSubscribers = MutableStateFlow(0)

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
    }
        .catch { cause ->
            if (cause is CancellationException) {
                // mark _executionState as inactive
            }
            throw cause
        }
        .shareIn(scope = scope, replay = 30000, started = SharingStarted.Eagerly)
//        .shareIn(scope = scope, replay = 30000, started = { subscriptionCount ->
//        subscriptionCount.map {
//            numOfScriptOutputSubscribers.value = it
//            if (it > 1) SharingCommand.START else SharingCommand.STOP
//        }
//    })



    override suspend fun runScript(command: String, arguments: List<String>) =
        executionRequestFlow.emit(ExecutionRequest.RunScript(command, arguments))

    override suspend fun stopScript() {
        _executionState.value = ExecutionState.STOPPING
        executionRequestFlow.emit(ExecutionRequest.StopScript)
    }
}

private sealed class ExecutionRequest {
    data class RunScript(val command: String, val args: List<String>) : ExecutionRequest()
    object StopScript : ExecutionRequest()
}
