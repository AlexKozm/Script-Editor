package com.example.scripteditor.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import com.example.scripteditor.core.models.ExecutionEvent
import com.example.scripteditor.domain.ExecutionErrorWithLink
import com.example.scripteditor.domain.parseExecutionErrorForScriptRef
import com.example.scripteditor.ui.LogLine
import com.example.scripteditor.ui.theme.LocalExtendedColorScheme
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ScriptOutput(
    modifier: Modifier = Modifier,
    state: SnapshotStateList<LogLine>,
    onErrLinkClick: ExecutionErrorWithLink.() -> Unit
) {
    Card (
        modifier = modifier.fillMaxSize()
    ) {
        val lazyListState = rememberLazyListState()
        var isAtBottom by remember { mutableStateOf(true) }

//        LaunchedEffect(lazyListState.firstVisibleItemIndex) {
//            val layoutInfo = lazyListState.layoutInfo
//            val visibleItems = layoutInfo.visibleItemsInfo
//
//            visibleItems.lastOrNull()?.index?.let { lastVisibleItemIndex ->
//                if (lastVisibleItemIndex == layoutInfo.totalItemsCount - 1) {
//                    isAtBottom = true
//                } else if (lazyListState.lastScrolledBackward) {
//                    isAtBottom = false
//                }
//            }
//        }
        // probably derivedStateOf is needed here
//        LaunchedEffect(state) {
//            snapshotFlow { state.lastOrNull() }
//                .filterNotNull()
//                .filter { isAtBottom }
////                .debounce(100L)
//                .collect {
//                    lazyListState.scrollToItem(it.index)
//                }
//        }

        WithAlwaysVisibleVerticalScrollbar(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            scrollState = rememberScrollbarAdapter(lazyListState),
        ) { leftPadding ->
//            SelectionContainer(
//                modifier = modifier.padding(end = leftPadding),
//            ) {
                val commonItemModifier = remember {
                    Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .graphicsLayer() // Кеширует отрисовку в слой
                        .padding(end = 16.dp)
                }
                LazyColumn(
                    state = lazyListState,
                ) {
                    items(
                        items = state,
                        key = { it.id },
                        contentType = { "text" }
                    ) {
                        val message = remember(it.id) { it.text }
                        BasicText(
                            modifier = commonItemModifier,
                            text = message,
                            softWrap = false,
                        )
                    }
                }
//            }
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
    val textStateList = remember { mutableStateListOf<LogLine>() }
    textStateList.addAll(list.withIndex().map { LogLine(it.index, it.value.toString()) })
    ScriptOutput(
        state = textStateList,
        onErrLinkClick = {}
    )
}