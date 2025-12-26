package com.example.scripteditor.core.components

import com.example.scripteditor.core.ExecutionState
import com.example.scripteditor.core.repositories.ExecutionEvent
import com.example.scripteditor.core.repositories.ScriptExecution
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import java.io.File

class ScriptControllerImpl(
    private val coroutineScope: CoroutineScope,
) : ScriptController {
    private var executionJob: Job? = null
    private val _scriptOutput: MutableSharedFlow<ExecutionEvent> = MutableSharedFlow()
    override val scriptOutput = _scriptOutput.withIndex()

    private val _executionState: MutableStateFlow<ExecutionState> = MutableStateFlow(ExecutionState.STOPPED)
    override val executionState: StateFlow<ExecutionState> get() = _executionState.asStateFlow()
    override fun saveScript(path: String, script: String) {
        val file = File(path)
        file.writeText(script) // TODO: handle exceptions
        file.setExecutable(true)
    }

    fun loadScript(path: String): String = File(path).readText() // TODO: handle exceptions


    override fun saveAndRun(path: String, script: String, scriptExecution: ScriptExecution) {
        stop()
        saveScript(path = path, script = script)
        executionJob = coroutineScope.launch(Dispatchers.Default) {
            _executionState.value = ExecutionState.RUNNING
            scriptExecution.run().collect(_scriptOutput::emit)
        }
        executionJob?.invokeOnCompletion { _executionState.value = ExecutionState.STOPPED }
    }

    override fun stop() {
        _executionState.value = ExecutionState.STOPPING
        executionJob?.cancel()
        executionJob = null
    }
}