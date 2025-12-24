package com.example.scripteditor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MainScreen(
    vm: MainScreenVM = viewModel(),
) {

    val coroutineScope = rememberCoroutineScope()
    val output by vm.scriptOutput.collectAsStateWithLifecycle("")
    val executionState by vm.executionState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row {
            Button(
                onClick = { vm.nextExecutionState() }
            ) {
                Text(text = executionState.toString())
            }
            Text(text = "Command: ")
            TextField(state = vm.command)
            Text(text = "path: ")
            TextField(state = vm.file)
        }
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            TextField(
                modifier = Modifier.weight(1f),
                state = vm.codeEditorState
            )
            Text(
                modifier = Modifier.weight(1f),
                text = output
            )

        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen()
}