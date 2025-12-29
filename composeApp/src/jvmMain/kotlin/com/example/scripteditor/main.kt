package com.example.scripteditor

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension

fun main() = application {
    Window(
        state = rememberWindowState(),
        onCloseRequest = ::exitApplication,
        title = "Script Editor",
    ) {
        val density = LocalDensity.current
        val minWidthPx = with(density) { 600.dp.roundToPx() }
        val minHeightPx = with(density) { 300.dp.roundToPx() }

        window.minimumSize = Dimension(minWidthPx, minHeightPx)
        App()
    }
}