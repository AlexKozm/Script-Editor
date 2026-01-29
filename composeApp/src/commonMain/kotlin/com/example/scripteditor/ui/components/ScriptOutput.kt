package com.example.scripteditor.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import com.example.scripteditor.core.ExecutionEvent
import com.example.scripteditor.domain.ExecutionErrorWithLink
import com.example.scripteditor.domain.parseExecutionErrorForScriptRef
import com.example.scripteditor.ui.theme.LocalExtendedColorScheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ScriptOutput(
    modifier: Modifier = Modifier,
    state: SnapshotStateList<IndexedValue<ExecutionEvent>>,
    onErrLinkClick: ExecutionErrorWithLink.() -> Unit
) {
    Card (
        modifier = modifier.fillMaxSize()
    ) {
        val lazyListState = rememberLazyListState()
        var isAtBottom by remember { mutableStateOf(true) }

        LaunchedEffect(lazyListState.firstVisibleItemIndex) {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo

            visibleItems.lastOrNull()?.index?.let { lastVisibleItemIndex ->
                if (lastVisibleItemIndex == layoutInfo.totalItemsCount - 1) {
                    isAtBottom = true
                } else if (lazyListState.lastScrolledBackward) {
                    isAtBottom = false
                }
            }
        }
        LaunchedEffect(state.lastOrNull()?.index) {
            if (isAtBottom) {
                state.lastOrNull()?.let {
                    lazyListState.scrollToItem(lazyListState.layoutInfo.totalItemsCount - 1)
                }
            }
        }

        WithAlwaysVisibleVerticalScrollbar(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            scrollState = rememberScrollbarAdapter(lazyListState),
        ) { leftPadding ->
            SelectionContainer(
                modifier = modifier.padding(end = leftPadding),
            ) {
                LazyColumn(
                    state = lazyListState,
                ) {
                    items(
                        items = state,
                        key = { it.index },
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = 16.dp),
                            text = it.value.getLine(onErrLinkClick),
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun ExecutionEvent.getLine(
    onErrLinkClick: ExecutionErrorWithLink.() -> Unit
) = when (this) {
    is ExecutionEvent.Finished -> buildAnnotatedString {
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.secondary)) {append("FINISHED. Code: $exitCode") }
    }
    is ExecutionEvent.StdOut -> buildAnnotatedString { append(line) }
    is ExecutionEvent.StdErr -> errAnnotatedString(onErrLinkClick)
    is ExecutionEvent.SystemError -> buildAnnotatedString {
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onErrorContainer)) { append("SYSTEM ERR: $message")  }
    }
}

@Composable
private fun ExecutionEvent.StdErr.errAnnotatedString(
    onErrLinkClick: ExecutionErrorWithLink.() -> Unit,
    errorColor: Color = MaterialTheme.colorScheme.error,
    keyWordsColor: Color = LocalExtendedColorScheme.current.keyWords.color
): AnnotatedString {
    val parsedError = parseExecutionErrorForScriptRef(
        errLine = line,
    )
    return if (parsedError == null) buildAnnotatedString {
        withStyle(style = SpanStyle(color = errorColor)) { append(line) }
    }
    else buildAnnotatedString {
        val link = LinkAnnotation.Clickable(
            tag = parsedError.link,
            styles = TextLinkStyles(SpanStyle(color = keyWordsColor)),
            linkInteractionListener = {
                parsedError.onErrLinkClick()
            }
        )
        withLink(link) { append(parsedError.link) }
        withStyle(style = SpanStyle(color = errorColor)) { append(parsedError.err) }
    }
}

@Preview
@Composable
private fun ScriptOutputPreview() {
    val list = listOf(
        ExecutionEvent.StdOut("result: 1"),
        ExecutionEvent.Finished(0),
        ExecutionEvent.StdErr("some-file.kts:2:3: error: Something")
    )
    val textStateList = remember { mutableStateListOf<IndexedValue<ExecutionEvent>>() }
    textStateList.addAll(list.withIndex())
    ScriptOutput(
        state = textStateList,
        onErrLinkClick = {}
    )
}