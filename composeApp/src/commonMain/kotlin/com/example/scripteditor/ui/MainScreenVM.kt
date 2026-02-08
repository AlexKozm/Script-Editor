package com.example.scripteditor.ui

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scripteditor.core.flow.batch.BatchCollector
import com.example.scripteditor.core.flow.batch.BatchFiller
import com.example.scripteditor.core.flow.batch.batched
import com.example.scripteditor.core.flow.batch.getAndClear
import com.example.scripteditor.core.flow.batch.sequential
import com.example.scripteditor.core.models.ExecutionEvent
import com.example.scripteditor.core.models.ExecutionState
import com.example.scripteditor.domain.LoadFileUseCase
import com.example.scripteditor.domain.SaveFileUseCase
import com.example.scripteditor.domain.ScriptStateHolderFactory
import com.example.scripteditor.domain.ScriptStateHolderFactoryImpl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus


class MainScreenVM(
    private val loadFileUseCase: LoadFileUseCase = LoadFileUseCase(),
    private val saveFileUseCase: SaveFileUseCase = SaveFileUseCase(),
    scriptStateHolderFactory: ScriptStateHolderFactory = ScriptStateHolderFactoryImpl()
) : ViewModel() {
    val codeEditorState: TextFieldState = TextFieldState()
    private val codeText get() = codeEditorState.text.toString()

    val mutableStateListOutput = mutableStateListOf<IndexedValue<ExecutionEvent>>()
    private val scriptStateHolder = scriptStateHolderFactory.create(
        viewModelScope + CoroutineExceptionHandler { _, throwable ->
            viewModelScope.launch {
                snackbarHostState.showSnackbar("${throwable.message}")
            }
        }
    )
        .apply {
            scriptOutput
                .withIndex()
                .batched(Dispatchers.Default,
                    fillBatch = BatchFiller.sequential(),
                    collectBatch = BatchCollector { mutableList ->
                        if (mutableStateListOutput.size > 10) {
                            mutableList.toList().also { mutableList.clear() }
                        } else {
                            listOf(mutableList.removeFirst())
                        }
                    },
                    fillContinueSuspender = { list, receiver ->
                        if (list.size > 1000) receiver.receive()
                    }
                )
                .map { batch ->
                    mutableStateListOutput.addAll(batch)
                    if (mutableStateListOutput.size > 30000) {
                        mutableStateListOutput.subList(0, mutableStateListOutput.size - 30000).clear()
                    }
                }
                .cancellable()
                .launchIn(viewModelScope)
        }


    val executionState: StateFlow<ExecutionState> = scriptStateHolder.executionState

    val snackbarHostState = SnackbarHostState()

    val commandWithFlagsTextFieldState: TextFieldState = TextFieldState("kotlinc -script")
    // TODO: make better parsing or make possible to split flags in ui
    private val command get() = commandWithFlagsTextFieldState.text.toString().substringBefore(" -")
    private val flag get() = commandWithFlagsTextFieldState.text.toString().substringAfter(" -").let { "-$it" }

    val filePathTextFieldState: TextFieldState = TextFieldState("foo.kts")
    private val filePath get() = filePathTextFieldState.text.toString()

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
        when (scriptStateHolder.executionState.value) {
            ExecutionState.STOPPED -> executeScript()
            ExecutionState.RUNNING -> scriptStateHolder.stopScript()
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
                mutableStateListOutput.clear()
                scriptStateHolder.runScript(command, listOf(flag, filePath))
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