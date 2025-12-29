package com.example.scripteditor.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import com.example.scripteditor.ui.theme.LocalExtendedColorScheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScriptEditor(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
) {
    Card (
        modifier = modifier.fillMaxSize()
    ) {
        val scrollState = rememberScrollState()
        WithAlwaysVisibleVerticalScrollbar(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            scrollState = rememberScrollbarAdapter(scrollState),
        ) { leftPadding ->
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = leftPadding + 4.dp),
                state = textFieldState,
                inputTransformation = codeEditorInputTransformation,
                outputTransformation = codeEditorOutputTransformation(),
                scrollState = scrollState,
            )
        }
    }
}

private val keyWords = setOf(
    "fun",
    "val",
    "var",
    "when",
    "while",
    "for",
    "class",
    "interface",
    "object",
    "this",
    "import"
)
private val regex = Regex(keyWords.joinToString("\\b|") + "\\b")

@Composable
fun codeEditorOutputTransformation(
    keyWordsColor: Color = LocalExtendedColorScheme.current.keyWords.color,
) = OutputTransformation {
    regex
        .findAll(asCharSequence())
        .map { it.range }
        .forEach { range ->
            addStyle(spanStyle = SpanStyle(color = keyWordsColor), start = range.first, end = range.last + 1)
        }
}

private val codeEditorInputTransformation = InputTransformation {
    if (asCharSequence().contains("\t")) {
        val allTabs = asCharSequence().toString()
            .withIndex()
            .filter { it.value == '\t' }
        val replaceTabWithString = "    "
        val newText = asCharSequence().toString().replace("\t", replaceTabWithString)
        val newCursorPlace = allTabs.last().index + allTabs.size * (replaceTabWithString.length - 1)
        replace(0, length, newText)
        placeCursorAfterCharAt(newCursorPlace)
    }
}

@Preview
@Composable
private fun PreviewCodeEditor() {
    val textFieldState = TextFieldState("""
        fun a() {
            var l = 1
        }
        println("hihi")
    """.trimIndent())
    ScriptEditor(textFieldState = textFieldState)
}