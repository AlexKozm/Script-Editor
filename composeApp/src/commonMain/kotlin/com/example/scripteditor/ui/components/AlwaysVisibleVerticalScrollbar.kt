package com.example.scripteditor.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WithAlwaysVisibleVerticalScrollbar(
    modifier: Modifier = Modifier,
    scrollState: ScrollbarAdapter,
    content: @Composable (leftPadding: Dp) -> Unit
) {
    val leftPadding = 8.dp
    Box (
        modifier = modifier
    ) {
        content(leftPadding)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(leftPadding)
                .background(Color.Black.copy(alpha = 0.1f))
        ) {
            VerticalScrollbar(
                adapter = scrollState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
            )
        }
    }
}

@Preview
@Composable
private fun WithAlwaysVisibleVerticalScrollbarEditor() {
    val scrollState = rememberScrollState()
    WithAlwaysVisibleVerticalScrollbar(
        scrollState = rememberScrollbarAdapter(scrollState),
    ) { leftPadding ->
        Text(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = leftPadding)
                .verticalScroll(scrollState),
            text = List(100) { "lorem ipsum dolor sit amet consectetur adipiscing elit." }
                .joinToString(" "),
        )
    }
}