package com.example.scripteditor

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.example.scripteditor.ui.theme.AppTheme
import com.example.scripteditor.ui.MainScreen


@Composable
fun App() {
    AppTheme {
        Surface {
            MainScreen()
        }
    }
}