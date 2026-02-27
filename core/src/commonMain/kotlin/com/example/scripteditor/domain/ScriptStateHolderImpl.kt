package com.example.scripteditor.domain

import com.example.scripteditor.core.models.ExecutionEvent
import com.example.scripteditor.core.models.ExecutionState
import com.example.scripteditor.data.ScriptExecutionRepository
import com.example.scripteditor.data.ScriptExecutionSequentialRepository
import com.example.scripteditor.data.ScriptExecutionStoppableRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.*
import kotlin.also
import kotlin.let

class ScriptStateHolderImpl(
    private val scriptExecutionRepository: ScriptExecutionStoppableRepository = ScriptExecutionSequentialRepository(),
    private val scope: CoroutineScope
) : ScriptStateHolder {
    private val _executionState: MutableStateFlow<ExecutionState> = MutableStateFlow(ExecutionState.STOPPED)
    override val executionState: StateFlow<ExecutionState> = _executionState

    private val executionRequestFlow: Channel<RunScript> = Channel(0, BufferOverflow.SUSPEND)
    private val stopSignal: MutableStateFlow<CompletableDeferred<Unit>> = MutableStateFlow(CompletableDeferred())

    override val scriptOutput: SharedFlow<ExecutionEvent> = channelFlow {
        executionRequestFlow.receiveAsFlow().collectLatest { request ->
            _executionState.value = ExecutionState.RUNNING
            try {
                with(request) {
                    val stopSignal = stopSignal.value.let {
                        if (it.isActive) it
                        else CompletableDeferred<Unit>().also { stopSignal.value = it }
                    }
                    scriptExecutionRepository.run(command, args, stopSignal).collect {
                        send(it)
                    }
                }
            } catch (_: ClosedReceiveChannelException) {}
            finally {
                _executionState.value = ExecutionState.STOPPED
            }
        }
    }
        .buffer(0)
//        .onEach { println("share $it") }
        .shareIn(scope = scope + Dispatchers.Default, replay = 0, started = SharingStarted.Eagerly)



    override fun runScript(command: String, arguments: List<String>) {
        scope.launch {
            executionRequestFlow.send(RunScript(command, arguments))
        }
    }

    override fun stopScript() {
        scope.launch {
            _executionState.value = ExecutionState.STOPPING
            stopSignal.value.complete(Unit)
        }
    }
}

data class RunScript(val command: String, val args: List<String>)
