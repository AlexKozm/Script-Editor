package com.example.scripteditor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scripteditor.ui.components.RunAndStopButton
import com.example.scripteditor.ui.components.ScriptEditor
import com.example.scripteditor.ui.components.ScriptOutput
import com.example.scripteditor.ui.components.SmallTextField
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MainScreen(
    vm: MainScreenVM = viewModel(),
) {

    val executionState by vm.executionState.collectAsStateWithLifecycle()
    val output = vm.mutableStateListOutput
    val coroutineScope = rememberCoroutineScope()


    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = vm.snackbarHostState)
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = { coroutineScope.launch { vm.loadScript() } }
                ){
                    Icon(Icons.Outlined.Download, contentDescription = "Open file")
                }
                IconButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = { coroutineScope.launch { vm.saveScript() } }
                ){
                    Icon(Icons.Outlined.Save, contentDescription = "Save")
                }
                RunAndStopButton(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = { coroutineScope.launch { vm.nextExecutionState() } },
                    state = executionState
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = "Command: "
                )
                SmallTextField(
                    modifier = Modifier.align(Alignment.CenterVertically)
                        .widthIn(50.dp, 300.dp),
                    state = vm.commandWithFlagsTextFieldState
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = "path: "
                )
                SmallTextField(
                    modifier = Modifier.align(Alignment.CenterVertically)
                        .widthIn(50.dp, 300.dp),
                    state = vm.filePathTextFieldState
                )
            }
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                val focusRequester = remember { FocusRequester() }
                ScriptEditor(
                    modifier = Modifier.weight(1f).focusRequester(focusRequester),
                    textFieldState = vm.codeEditorState,
                )
                Spacer(Modifier.width(4.dp))
                ScriptOutput(
                    modifier = Modifier.weight(1f),
                    state = output,
                    textFieldState = vm.codeEditorState,
                    focusRequester = focusRequester
                )

            }
        }
    }


}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen()
}