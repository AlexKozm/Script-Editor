package com.example.scripteditor

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.scripteditor.ui.MainScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        MainScreen()
    }
}