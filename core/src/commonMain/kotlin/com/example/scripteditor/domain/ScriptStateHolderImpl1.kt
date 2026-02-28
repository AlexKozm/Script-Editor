package com.example.scripteditor.domain

import com.example.scripteditor.core.models.ExecutionEvent
import com.example.scripteditor.core.models.ExecutionState
import com.example.scripteditor.data.ScriptExecutionSequentialRepository
import com.example.scripteditor.data.ScriptExecutionStoppableRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class ScriptStateHolderImpl1(
    private val scriptExecutionRepository: ScriptExecutionStoppableRepository = ScriptExecutionSequentialRepository(),
    private val scope: CoroutineScope,
    exceptionHandler: (e: Throwable) -> Unit = { e -> if(e is CancellationException) throw e }
) : ScriptStateHolder {

    private val scriptExecutionRequestChannel: Channel<ExecutionRequest> = Channel()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scriptExecutionStateFlow: SharedFlow<ScriptExecutionState> =
        scriptExecutionRequestChannel.consumeAsFlow().distinctUntilChanged()
            .transformLatest { request ->
                when(request) {
                    is ExecutionRequest.RunScript -> {
                        emit(ScriptExecutionState.RunningScript(request.command, request.args))
                        // In order to work emit should wait for all subscribers to process a value.
                        // In current implementation of sharedFlow this works not this way.
                        // I see that emit is implemented more like send (to Channel),
                        // which awakes as soon as all subscribers got a value (but not processed it)
                        emit(ScriptExecutionState.StoppedScript)
                    }
                    ExecutionRequest.StopScript -> {
                        emit(ScriptExecutionState.StoppingScript)
                        emit(ScriptExecutionState.StoppedScript)
                    }
                }
            }
            .shareIn(scope, SharingStarted.Eagerly, replay = 0)


    override val scriptOutput: SharedFlow<ExecutionEvent> = scriptExecutionStateFlow
        .filterIsInstance<ScriptExecutionState.RunningScript>()
        .transform { emitAll(scriptExecutionRepository.run(it.command, it.args, it.stopSignal)) }
        .catch { exceptionHandler(it) }
        .shareIn(scope, SharingStarted.Eagerly, replay = 0)


    override val executionState: StateFlow<ExecutionState> = scriptExecutionStateFlow
        .completeRunningScriptOnStopRequest()
        .map(ScriptExecutionState::toExecutionState)
        .stateIn(scope, SharingStarted.Eagerly, ExecutionState.STOPPED)


    override fun runScript(command: String, arguments: List<String>) {
        scope.launch {
            scriptExecutionRequestChannel.send(ExecutionRequest.RunScript(command, arguments))
        }
    }


    override fun stopScript() {
        scope.launch {
            scriptExecutionRequestChannel.send(ExecutionRequest.StopScript)
        }
    }

    private fun SharedFlow<ScriptExecutionState>.completeRunningScriptOnStopRequest() = this
        .pairScan(null to ScriptExecutionState.StoppedScript)
        .transform { (prev, cur) ->
            if (prev is ScriptExecutionState.RunningScript && cur is ScriptExecutionState.StoppingScript) {
                prev.stopSignal.complete(Unit)
            }
            emit(cur)
        }

}

private sealed class ScriptExecutionState {
    object StoppedScript: ScriptExecutionState()
    class RunningScript(
        val command: String,
        val args: List<String>,
        val stopSignal: CompletableDeferred<Unit> = CompletableDeferred()
    ): ScriptExecutionState()
    object StoppingScript: ScriptExecutionState()

    fun toExecutionState() = when(this) {
        is RunningScript -> ExecutionState.RUNNING
        StoppedScript -> ExecutionState.STOPPING
        StoppingScript -> ExecutionState.STOPPED
    }
}

private sealed class ExecutionRequest {
    data class RunScript(val command: String, val args: List<String>) : ExecutionRequest()
    object StopScript : ExecutionRequest()
}

fun <T: R, R> Flow<T>.pairScan(initial: Pair<R, T>) = scan(initial) { prev, new -> prev.second to new }

