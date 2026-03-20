package com.example.scripteditor.domain

import com.example.scripteditor.core.flow.ValueOrPlug
import com.example.scripteditor.core.flow.dropPlugs
import com.example.scripteditor.core.flow.shareInSequential
import com.example.scripteditor.core.models.ExecutionEvent
import com.example.scripteditor.core.models.ExecutionState
import com.example.scripteditor.data.ScriptExecutionRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

class ScriptStateHolderImpl1(
    private val scriptExecutionRepository: ScriptExecutionRepository,
    private val scope: CoroutineScope,
    exceptionHandler: (e: Throwable) -> Unit = { e -> throw e }
) : ScriptStateHolder {

    private val scriptExecutionRequestChannel: Channel<ExecutionRequest> = Channel()

    private val scriptExecutionState: MutableSharedFlow<ScriptExecutionState> = MutableSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scriptExecutionStateFlow: SharedFlow<ValueOrPlug<ScriptExecutionState>> =
        scriptExecutionRequestChannel.receiveAsFlow()
            .transformLatest { request ->
                when(request) {
                    is ExecutionRequest.RunScript -> {
                        emit(ScriptExecutionState.RunningScript(request.command, request.args))
                        emit(ScriptExecutionState.StoppedScript)
                    }
                    ExecutionRequest.StopScript -> {
                        println("Emitting StoppingScript")
                        emit(ScriptExecutionState.StoppingScript)
                        println("StoppingScript Emitted ")
                        emit(ScriptExecutionState.StoppedScript)
                    }
                }
            }
            .buffer(0)
            .shareInSequential(scope)

    /*
    ------+------
          |
          +------
     */


    override val scriptOutput: SharedFlow<ExecutionEvent> = scriptExecutionStateFlow
        .dropPlugs()
        .filterIsInstance<ScriptExecutionState.RunningScript>()
        .transform { emitAll(scriptExecutionRepository.run(it.command, it.args, it.stopSignal)) }
        .catch { exceptionHandler(it) }
        .shareIn(scope, SharingStarted.Eagerly, replay = 0)


    override val executionState: StateFlow<ExecutionState> = scriptExecutionStateFlow
        .dropPlugs()
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
}

fun Flow<ScriptExecutionState>.completeRunningScriptOnStopRequest() = this
    .pairScan(null to ScriptExecutionState.StoppedScript)
    .transform { (prev, cur) ->
        if (prev is ScriptExecutionState.RunningScript && cur is ScriptExecutionState.StoppingScript) {
            prev.stopSignal.complete(Unit)
        }
        emit(cur)
    }

sealed class ScriptExecutionState {
    object StoppedScript: ScriptExecutionState()
    class RunningScript(
        val command: String,
        val args: List<String>,
        val stopSignal: CompletableDeferred<Unit> = CompletableDeferred()
    ): ScriptExecutionState()
    object StoppingScript: ScriptExecutionState()

    fun toExecutionState() = when(this) {
        is RunningScript -> ExecutionState.RUNNING
        StoppingScript -> ExecutionState.STOPPING
        StoppedScript -> ExecutionState.STOPPED
    }
}

sealed class ExecutionRequest {
    data class RunScript(val command: String, val args: List<String>) : ExecutionRequest()
    object StopScript : ExecutionRequest()
}

fun <T: R, R> Flow<T>.pairScan(initial: Pair<R, T>) = scan(initial) { prev, new -> prev.second to new }

