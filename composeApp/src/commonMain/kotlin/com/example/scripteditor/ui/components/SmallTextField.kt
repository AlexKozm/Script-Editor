package com.example.scripteditor.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SmallTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderWidth by animateDpAsState(if (isFocused) 2.dp else 1.dp)

    BasicTextField(
        modifier = modifier,
        state = state,
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        lineLimits = TextFieldLineLimits.SingleLine,
        interactionSource = interactionSource,
        decorator = TextFieldDecorator { textComposable ->
            Card {
                Box(
                    modifier = Modifier
                        .border(
                            borderWidth,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.shapes.medium
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    textComposable()
                }
            }
        }
    )
}

@Preview
@Composable
private fun SmallTextFieldPreview() {
    SmallTextField(
        state = TextFieldState("kotlinc -script")
    )
}