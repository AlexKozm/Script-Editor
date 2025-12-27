package com.example.scripteditor.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scripteditor.core.ExecutionState
import com.example.scripteditor.core.ExecutionEvent
import com.example.scripteditor.domain.LoadFileUseCase
import com.example.scripteditor.domain.SaveFileUseCase
import com.example.scripteditor.domain.ScriptService
import com.example.scripteditor.domain.ScriptServiceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield


class MainScreenVM(
    private val scriptService: ScriptService = ScriptServiceImpl(),
    private val loadFileUseCase: LoadFileUseCase = LoadFileUseCase(),
    private val saveFileUseCase: SaveFileUseCase = SaveFileUseCase(),
) : ViewModel() {
    val codeEditorState: TextFieldState = TextFieldState()
    val codeText get() = codeEditorState.text.toString()
    val mutableStateListOutput = mutableStateListOf<IndexedValue<ExecutionEvent>>()

    init {
        viewModelScope.launch {
            scriptService.indexedScriptOutput.withIndex().flowOn(Dispatchers.Default).collect { event ->
                mutableStateListOutput.add(event)
                if (mutableStateListOutput.size > 30000) mutableStateListOutput.removeFirst()
                yield()
            }
        }
    }

    val executionState: StateFlow<ExecutionState> get() = scriptService.executionState

    val snackbarHostState = SnackbarHostState()

    val commandWithFlagsTextFieldState: TextFieldState = TextFieldState("kotlinc -script")
    // TODO: make better parsing or make possible to split flags in ui
    val command get() = commandWithFlagsTextFieldState.text.toString().substringBefore(" -")
    val flag get() = commandWithFlagsTextFieldState.text.toString().substringAfter(" -").let { "-$it" }

    val filePathTextFieldState: TextFieldState = TextFieldState("foo.kts")
    val filePath get() = filePathTextFieldState.text.toString()

    suspend fun loadScript(filePath: String = this.filePath) {
        loadFileUseCase(filePath)
            .onSuccess { text ->
                codeEditorState.setTextAndPlaceCursorAtEnd(text)
            }
            .onFailure { e ->
                snackbarHostState.showSnackbar("Could not load file $filePath. Exception: ${e.message}")
            }
    }

    suspend fun nextExecutionState() {
        when (scriptService.executionState.value) {
            ExecutionState.STOPPED -> executeScript()
            ExecutionState.RUNNING -> scriptService.stopScript()
            ExecutionState.STOPPING -> {}
        }
    }

    private suspend fun executeScript(
        command: String = this.command,
        flag: String = this.flag,
        filePath: String = this.filePath,
        data: String = this.codeText
    ) {
        saveScript(filePath, data)
            .onSuccess {
                scriptService.runScript(command, listOf(flag, filePath))
            }
    }

    suspend fun saveScript(
        filePath: String = this.filePath,
        data: String = this.codeText
    ): Result<Unit> = saveFileUseCase(filePath, data)
        .onFailure { e ->
            snackbarHostState.showSnackbar("Could not load file $filePath. Exception: ${e.message}")
        }

}