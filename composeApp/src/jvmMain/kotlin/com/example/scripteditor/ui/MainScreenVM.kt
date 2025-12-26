package com.example.scripteditor.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scripteditor.core.ExecutionState
import com.example.scripteditor.core.components.ScriptControllerImpl
import com.example.scripteditor.core.repositories.ExecutionEvent
import com.example.scripteditor.core.repositories.ScriptExecution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield


class MainScreenVM : ViewModel() {
    val scriptController: ScriptControllerImpl = ScriptControllerImpl(viewModelScope)
    val codeEditorState: TextFieldState = TextFieldState()
    val mutableStateListOutput = mutableStateListOf<IndexedValue<ExecutionEvent>>()

    init {
        viewModelScope.launch {
            scriptController.scriptOutput.flowOn(Dispatchers.Default).collect { event ->
                mutableStateListOutput.add(event)
                if (mutableStateListOutput.size > 30000) mutableStateListOutput.removeFirst()
                yield()
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