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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.example.scripteditor.core.repositories.ExecutionEvent
import com.example.scripteditor.core.repositories.ExecutionEvent.Finished
import com.example.scripteditor.core.repositories.ExecutionEvent.StdErr
import com.example.scripteditor.core.repositories.ExecutionEvent.StdOut
import com.example.scripteditor.core.repositories.ExecutionEvent.SystemError
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ScriptOutput(
    modifier: Modifier = Modifier,
    state: SnapshotStateList<IndexedValue<ExecutionEvent>>,
    focusRequester: FocusRequester,
    textFieldState: TextFieldState
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
                            text = it.value.getLine(textFieldState, focusRequester),
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

fun ExecutionEvent.getLine(
    textFieldState: TextFieldState,
    focusRequester: FocusRequester
) = when (this) {
    is Finished -> buildAnnotatedString { append("FINISHED. Code: $exitCode") }
    is StdOut -> buildAnnotatedString { append("Out: $line") }
    is StdErr -> {
        val regex = Regex(""":(\d+):(\d+):""")
        val res = regex.find(line)

        buildAnnotatedString {
            if (res == null) {
                append(line)
                return@buildAnnotatedString
            }

            val lineNum = res.groups[1]?.value?.toIntOrNull()
            val charNum = res.groups[2]?.value?.toIntOrNull()

            val file = res.groups[0]?.range?.first?.let { line.slice(0 until it) }
            val err = res.groups[0]?.range?.last?.let { line.slice(it until line.length) }
            val tag = "$file:$lineNum:$charNum"

            val link = LinkAnnotation.Clickable(
                tag = tag,
                styles = TextLinkStyles(SpanStyle(color = Color.Blue)),
                linkInteractionListener = {
                    textFieldState.edit {
                        if (lineNum != null && charNum != null) {
                            val place = originalText.toString()
                                .split("\n")
                                .take(lineNum)
                                .mapIndexed { index, string ->
                                    if (index != lineNum - 1) string.length else charNum
                                }
                                .sum()
                            placeCursorAfterCharAt(place) // TODO: catch
                        }
                    }
                    focusRequester.requestFocus()
                }
            )
            withLink(link) { append(tag) }
            append(err)
        }
    }
    is SystemError -> buildAnnotatedString { append("ERR: $message") }
}

@Preview
@Composable
private fun ScriptOutputPreview() {
//    ScriptOutput(text = """
//        Some output
//        of this program
//    """.trimIndent())
}