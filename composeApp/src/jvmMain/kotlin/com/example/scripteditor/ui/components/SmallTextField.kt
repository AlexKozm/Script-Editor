package com.example.scripteditor.ui.components

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SmallTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
) {
    OutlinedTextField(
        modifier = modifier,
        state = state,
        lineLimits = TextFieldLineLimits.SingleLine,
    )
}