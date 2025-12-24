package com.example.scripteditor

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "scripteditor",
    ) {
        App()
    }
}