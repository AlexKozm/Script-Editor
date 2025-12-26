package com.example.scripteditor.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RunCircle
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
    val icon = when (state) {
        ExecutionState.STOPPED -> Icons.Outlined.PlayArrow
        ExecutionState.RUNNING -> Icons.Outlined.Stop
        ExecutionState.STOPPING -> Icons.Outlined.Pending
    }
    val enabled = state != ExecutionState.STOPPING
    IconButton(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick
    ) {
        Icon(icon, contentDescription = "next script execution state")
    }
}

@Preview
@Composable
fun RunAndStopButtonPreviewRun() {
    RunAndStopButton(onClick = {}, state = ExecutionState.STOPPED)
}

@Preview
@Composable
fun RunAndStopButtonPreviewStop() {
    RunAndStopButton(onClick = {}, state = ExecutionState.RUNNING)
}

@Preview
@Composable
fun RunAndStopButtonPreviewPending() {
    RunAndStopButton(onClick = {}, state = ExecutionState.STOPPING)
}