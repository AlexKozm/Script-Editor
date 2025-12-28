package com.example.scripteditor.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scripteditor.core.ExecutionEvent
import com.example.scripteditor.core.ExecutionState
import com.example.scripteditor.domain.getCursorPlace
import com.example.scripteditor.ui.components.ScriptEditor
import com.example.scripteditor.ui.components.ScriptOutput
import com.example.scripteditor.ui.components.TopBar
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MainScreen(
    vm: MainScreenVM = viewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val executionState by vm.executionState.collectAsStateWithLifecycle()

    MainScreen(
        onOpenFileClick = { coroutineScope.launch { vm.loadScript() } },
        onSaveClick = { coroutineScope.launch { vm.saveScript() } },
        onRunOrStopClick = { coroutineScope.launch { vm.nextExecutionState() } },
        executionState = executionState,
        commandTextFieldState = vm.commandWithFlagsTextFieldState,
        fileTextFieldState = vm.filePathTextFieldState,
        snackbarHostState = vm.snackbarHostState,
        mutableStateListOutput = vm.mutableStateListOutput,
        codeEditorState = vm.codeEditorState,
    )
}

@Composable
fun MainScreen(
    onOpenFileClick: () -> Unit,
    onSaveClick: () -> Unit,
    onRunOrStopClick: () -> Unit,
    executionState: ExecutionState,
    commandTextFieldState: TextFieldState,
    fileTextFieldState: TextFieldState,

    snackbarHostState: SnackbarHostState,
    mutableStateListOutput: SnapshotStateList<IndexedValue<ExecutionEvent>>,
    codeEditorState: TextFieldState
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopBar(
                onOpenFileClick = onOpenFileClick,
                onSaveClick = onSaveClick,
                onRunOrStopClick = onRunOrStopClick,
                executionState = executionState,
                commandTextFieldState = commandTextFieldState,
                fileTextFieldState = fileTextFieldState,
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            val scriptFocusRequester = remember { FocusRequester() }
            ScriptEditor(
                modifier = Modifier.weight(1f).focusRequester(scriptFocusRequester),
                textFieldState = codeEditorState,
            )
            Spacer(Modifier.width(4.dp))
            ScriptOutput(
                modifier = Modifier.weight(1f),
                state = mutableStateListOutput,
                onErrLinkClick = {
                    try {
                        val place = getCursorPlace(codeEditorState.text.toString())
                        if (place != null) {
                            codeEditorState.edit { placeCursorAfterCharAt(place) }
                            scriptFocusRequester.requestFocus()
                        }
                    } catch (_: IllegalArgumentException) {
                    }
                }
            )
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    val list = listOf(
        ExecutionEvent.StdOut("result: 1"),
        ExecutionEvent.Finished(0),
        ExecutionEvent.StdErr("some-file.kts:2:3: error: Something")
    )
    val textFieldState = TextFieldState("""
        fun a() {
            var l = 1
        }
        println("hihi")
    """.trimIndent())

    MainScreen(
        onOpenFileClick = {},
        onSaveClick = {},
        onRunOrStopClick = {},
        executionState = ExecutionState.STOPPED,
        commandTextFieldState = rememberTextFieldState("kotlinc -script"),
        fileTextFieldState = rememberTextFieldState("foo.kts"),
        snackbarHostState = remember { SnackbarHostState() },
        mutableStateListOutput = remember {
            mutableStateListOf<IndexedValue<ExecutionEvent>>().apply { addAll(list.withIndex()) }
        },
        codeEditorState = textFieldState,
    )
}