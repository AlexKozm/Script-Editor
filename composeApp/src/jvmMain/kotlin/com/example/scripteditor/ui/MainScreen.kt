package com.example.scripteditor.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scripteditor.ui.components.ScriptEditor
import com.example.scripteditor.ui.components.ScriptOutput
import com.example.scripteditor.ui.components.SmallTextField
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MainScreen(
    vm: MainScreenVM = viewModel(),
) {

    val output by vm.scriptOutput.collectAsStateWithLifecycle(emptyList())
    val executionState by vm.executionState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row {
            Button(
                modifier = Modifier.alignByBaseline(),
                onClick = { vm.nextExecutionState() }
            ) {
                Text(
                    text = executionState.toString()
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                modifier = Modifier.alignByBaseline(),
                text = "Command: "
            )
            SmallTextField(
                modifier = Modifier.alignByBaseline()
                    .widthIn(50.dp, 300.dp),
                state = vm.command
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                modifier = Modifier.alignByBaseline(),
                text = "path: "
            )
            SmallTextField(
                modifier = Modifier.alignByBaseline()
                    .widthIn(50.dp, 300.dp),
                state = vm.file
            )
        }
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            ScriptEditor(
                modifier = Modifier.weight(1f),
                textFieldState = vm.codeEditorState,
            )
            Spacer(Modifier.width(4.dp))
            ScriptOutput(
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