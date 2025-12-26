package com.example.scripteditor.ui.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.scripteditor.core.repositories.ExecutionEvent
import com.example.scripteditor.core.repositories.getLine
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ScriptOutput(
    modifier: Modifier = Modifier,
    state: SnapshotStateList<IndexedValue<ExecutionEvent>>,
) {
    Card (
        modifier = modifier.fillMaxSize()
    ) {
        val scrollState = rememberLazyListState()
        Box (
            modifier = Modifier.fillMaxSize()
                .padding(8.dp)
        ) {
            SelectionContainer {
                LazyColumn(
                    state = scrollState,
                ) {
                    items(
                        items = state,
                        key = { it.index },
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = 16.dp),
                            text = it.value.getLine(),
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.05f))
            ) {
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Preview
@Composable
private fun ScriptOutputPreview() {
//    ScriptOutput(text = """
//        Some output
//        of this program
//    """.trimIndent())
}