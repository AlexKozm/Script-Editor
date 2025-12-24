package com.example.scripteditor.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScriptRunnerStateHolderImpl(
    override val command: String,
    override val arguments: List<String>,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ScriptRunnerStateHolder {
    val processBuilder = ProcessBuilder(command, *arguments.toTypedArray())

    private val _scriptRunningState: StateFlow<ExecutionState> = MutableStateFlow(ExecutionState.STOPPED)
    override val scriptRunningState: Flow<ExecutionState> = _scriptRunningState

    private val _scriptOutput: StateFlow<String> = MutableStateFlow("")
    override val scriptOutput: Flow<String> = _scriptOutput

    private val _scriptErrors: StateFlow<String> = MutableStateFlow("")
    override val scriptErrors: Flow<String> = _scriptErrors

    override suspend fun run() {

    }

//    private fun runOutputReader() {
//        process
//    }

    override suspend fun cancel() {
        TODO("Not yet implemented")
    }
}