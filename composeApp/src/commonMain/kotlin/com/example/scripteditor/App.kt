package com.example.scripteditor

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.example.scripteditor.ui.MainScreen

@Composable
fun App() {
    MaterialTheme {
        Surface {
            MainScreen()
        }
    }
}