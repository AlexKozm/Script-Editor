package com.example.scripteditor.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.buildAnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scripteditor.ScriptRunner
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ExecutionState {
    STOPPED,
    RUNNING,
    STOPPING,
}

class MainScreenVM : ViewModel() {
    val codeEditorState: TextFieldState = TextFieldState()
    private val _scriptOutput: MutableStateFlow<String> = MutableStateFlow("")
    val scriptOutput: Flow<String> get() = _scriptOutput.asStateFlow()

    val command: TextFieldState = TextFieldState("kotlinc -script")

    val file: TextFieldState = TextFieldState("foo.kts")

    private val _executionState: MutableStateFlow<ExecutionState> = MutableStateFlow(ExecutionState.STOPPED)
    val executionState: StateFlow<ExecutionState> get() = _executionState.asStateFlow()

    var executionJob: Job? = null

    fun nextExecutionState() {
        when (_executionState.value) {
            ExecutionState.STOPPED -> executeScript()
            ExecutionState.RUNNING -> stopScript()
            ExecutionState.STOPPING -> {}
        }
    }

    private fun stopScript() {
        _executionState.value = ExecutionState.STOPPING
        executionJob?.cancel()
    }

    private fun executeScript() {
        executionJob = viewModelScope.launch {
            _executionState.value = ExecutionState.RUNNING
            ScriptRunner(command = command.text.toString(), filePath = file.text.toString())
                .saveAndRun(codeEditorState.text.toString())
                .collect { newLine ->  _scriptOutput.update { "$it\n$newLine" } }
        }
        executionJob?.invokeOnCompletion { _executionState.value = ExecutionState.STOPPED }
    }
}