package com.example.scripteditor.domain

import com.example.scripteditor.core.flow.channelFlowToSequential
import com.example.scripteditor.core.flow.dropPlugs
import com.example.scripteditor.core.flow.shareInSequential
import com.example.scripteditor.core.models.ExecutionEvent
import com.example.scripteditor.core.models.ExecutionState
import com.example.scripteditor.data.ScriptExecutionRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

class ScriptStateHolderImpl2(
    private val scriptExecutionRepository: ScriptExecutionRepository,
    private val scope: CoroutineScope,
    exceptionHandler: (e: Throwable) -> Unit = { e -> throw e }
) : ScriptStateHolder {

    private val executionRequestSharedFlow = MutableSharedFlow<ExecutionRequest>(
        onBufferOverflow = BufferOverflow.DROP_LATEST,
        extraBufferCapacity = 1
    )

    private val runScriptSharedFlow =
        executionRequestSharedFlow.filterIsInstance<ExecutionRequest.RunScript>()
        .transform { request ->
            emit(ScriptExecutionState.RunningScript(request.command, request.args))
            emit(ScriptExecutionState.StoppedScript)
        }
            .channelFlowToSequential {
                buffer(0).shareIn(scope, SharingStarted.Eagerly)
            }
//        .shareInSequential(scope)
//        .dropPlugs()

    private val stopScriptFlow =
        executionRequestSharedFlow.filterIsInstance<ExecutionRequest.StopScript>()
        .map { ScriptExecutionState.StoppingScript }
        .shareIn(scope, SharingStarted.Eagerly, replay = 0)

    override val scriptOutput: SharedFlow<ExecutionEvent> = runScriptSharedFlow
        .filterIsInstance<ScriptExecutionState.RunningScript>()
        .transform { emitAll(scriptExecutionRepository.run(it.command, it.args, it.stopSignal)) }
        .catch { exceptionHandler(it) }
        .buffer(64, BufferOverflow.SUSPEND)
        .shareIn(scope, SharingStarted.Eagerly, replay = 0)

    override val executionState: StateFlow<ExecutionState> = merge(
        runScriptSharedFlow, stopScriptFlow
    )
        .buffer(0)
        .completeRunningScriptOnStopRequest()
        .map { it.toExecutionState() }
        .stateIn(scope, SharingStarted.Eagerly, ExecutionState.STOPPED)


    override fun runScript(command: String, arguments: List<String>) {
        scope.launch {
            executionRequestSharedFlow.emit(ExecutionRequest.RunScript(command, arguments))
        }
    }


    override fun stopScript() {
        scope.launch {
            executionRequestSharedFlow.emit(ExecutionRequest.StopScript)
        }
    }
}

