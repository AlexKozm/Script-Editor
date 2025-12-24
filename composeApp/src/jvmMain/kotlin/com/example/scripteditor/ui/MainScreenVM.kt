package com.example.scripteditor.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scripteditor.core.ExecutionState
import com.example.scripteditor.core.components.ScriptController
import com.example.scripteditor.core.components.ScriptControllerImpl
import com.example.scripteditor.core.repositories.ExecutionEvent
import com.example.scripteditor.core.repositories.ScriptExecution
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map


class MainScreenVM : ViewModel() {
    val scriptController: ScriptController = ScriptControllerImpl(viewModelScope)
    val codeEditorState: TextFieldState = TextFieldState()
    val scriptOutput: Flow<String> get() = scriptController.scriptOutput.map {
        it.joinToString("\n") { event ->
            when (event) {
                is ExecutionEvent.Finished -> "FINISHED. Code: ${event.exitCode}"
                is ExecutionEvent.StdErr -> event.line
                is ExecutionEvent.StdOut -> event.line
                is ExecutionEvent.SystemError -> "ERR: ${event.message}"
            }
        }
    }
    val executionState: StateFlow<ExecutionState> get() = scriptController.executionState


    val command: TextFieldState = TextFieldState("kotlinc -script")
    val file: TextFieldState = TextFieldState("foo.kts")


    fun nextExecutionState() {
        when (scriptController.executionState.value) {
            ExecutionState.STOPPED -> executeScript()
            ExecutionState.RUNNING -> scriptController.stop()
            ExecutionState.STOPPING -> {}
        }
    }

    private fun executeScript() {
        scriptController.saveAndRun(
            path = file.text.toString(),
            script = codeEditorState.text.toString(),
            scriptExecution = ScriptExecution(
                command = "kotlinc",
                arguments = listOf("-script", file.text.toString())
            )
        )
    }
}