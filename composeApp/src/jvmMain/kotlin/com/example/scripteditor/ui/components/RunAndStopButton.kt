package com.example.scripteditor.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.scripteditor.core.ExecutionState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RunAndStopButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    state: ExecutionState,
) {
    val (icon, description) = when (state) {
        ExecutionState.STOPPED -> Icons.Outlined.PlayArrow to "Run script"
        ExecutionState.RUNNING -> Icons.Outlined.Stop to "Stop script"
        ExecutionState.STOPPING -> Icons.Outlined.Pending to "Script is stopping"
    }

    IconButton(
        modifier = modifier,
        enabled = state != ExecutionState.STOPPING,
        onClick = onClick
    ) {
        Icon(icon, contentDescription = description)
    }
}

@Preview
@Composable
private fun RunAndStopButtonPreviewRun() {
    RunAndStopButton(onClick = {}, state = ExecutionState.STOPPED)
}

@Preview
@Composable
private fun RunAndStopButtonPreviewStop() {
    RunAndStopButton(onClick = {}, state = ExecutionState.RUNNING)
}

@Preview
@Composable
private fun RunAndStopButtonPreviewPending() {
    RunAndStopButton(onClick = {}, state = ExecutionState.STOPPING)
}