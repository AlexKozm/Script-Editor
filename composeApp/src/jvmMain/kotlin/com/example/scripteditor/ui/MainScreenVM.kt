package com.example.scripteditor.ui

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldState
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

    val keyWords = setOf("fun", "val", "var", "when", "while", "for", "class", "interface", "object", "this")
    val regex = Regex(keyWords.joinToString("\\b|") + "\\b")
    val codeEditorOutputTransformation = OutputTransformation {
        regex
            .findAll(asCharSequence())
            .map { it.range }
            .forEach { range ->
                addStyle(spanStyle = SpanStyle(color = Color.Blue), start = range.first, end = range.last + 1)
            }
    }

    val codeEditorInputTransformation = InputTransformation {
        if (asCharSequence().contains("\t")) {
            val newText = asCharSequence().toString().replace("\t", "    ")
            replace(0, length, newText)
        }
    }

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