package com.example.scripteditor.ui

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
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
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext


class MainScreenVM : ViewModel() {
    val scriptController: ScriptControllerImpl = ScriptControllerImpl(viewModelScope)
    val codeEditorState: TextFieldState = TextFieldState()
    val scriptOutput: Flow<List<ExecutionEvent>> get() = scriptController.scriptOutput

    val a = mutableStateListOf<ExecutionEvent>()

    init {
        viewModelScope.launch {
            scriptController.sharedFlow.collect { event ->
                event?.let {
                    a.add(event)
                    if (a.size > 1000) a.removeFirst()
                }
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