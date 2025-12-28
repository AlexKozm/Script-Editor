package com.example.scripteditor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TextWithSmallTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    text: String,
) {
    Row(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            modifier = Modifier.alignByBaseline(),
            text = text
        )
        SmallTextField(
            modifier = Modifier.alignByBaseline()
                .widthIn(50.dp, 300.dp),
            state = state
        )
    }
}


@Preview
@Composable
private fun TextWithSmallTextFieldPreview() {
    TextWithSmallTextField(
        text = "Command: ",
        state = TextFieldState("kotlinc -script")
    )
}