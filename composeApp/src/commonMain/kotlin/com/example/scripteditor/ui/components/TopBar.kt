package com.example.scripteditor.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.scripteditor.core.ExecutionState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TopBar(
    onOpenFileClick: () -> Unit,
    onSaveClick: () -> Unit,
    onRunOrStopClick: () -> Unit,
    executionState: ExecutionState,
    commandTextFieldState: TextFieldState,
    fileTextFieldState: TextFieldState,
) {
    Row {
        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = onOpenFileClick
        ){
            Icon(Icons.Outlined.Download, contentDescription = "Open file")
        }
        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = onSaveClick
        ){
            Icon(Icons.Outlined.Save, contentDescription = "Save")
        }
        RunAndStopButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = onRunOrStopClick,
            state = executionState
        )
        Spacer(modifier = Modifier.width(10.dp))
        TextWithSmallTextField(
            modifier = Modifier.align(Alignment.CenterVertically),
            state = commandTextFieldState,
            text = "Command: "
        )
        TextWithSmallTextField(
            modifier = Modifier.align(Alignment.CenterVertically),
            state = fileTextFieldState,
            text = "path: "
        )
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    TopBar(
        onOpenFileClick = {},
        onSaveClick = {},
        onRunOrStopClick = {},
        executionState = ExecutionState.STOPPED,
        commandTextFieldState = rememberTextFieldState("kotlinc -script"),
        fileTextFieldState = rememberTextFieldState("foo.kts"),
    )
}