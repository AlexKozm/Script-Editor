package com.example.scripteditor.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
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
        Box (
            modifier = Modifier.fillMaxSize()
                .padding(8.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 16.dp),
                state = textFieldState,
                inputTransformation = codeEditorInputTransformation,
                outputTransformation = codeEditorOutputTransformation,
                scrollState = scrollState,
            )
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
    "this"
)
private val regex = Regex(keyWords.joinToString("\\b|") + "\\b")
private val codeEditorOutputTransformation = OutputTransformation {
    regex
        .findAll(asCharSequence())
        .map { it.range }
        .forEach { range ->
            addStyle(spanStyle = SpanStyle(color = Color.Blue), start = range.first, end = range.last + 1)
        }
}

// TODO: works laggy. Delete or fix
private val codeEditorInputTransformation = InputTransformation {
    if (asCharSequence().contains("\t")) {
        val newText = asCharSequence().toString().replace("\t", "    ")
        replace(0, length, newText)
    }
}

@Preview
@Composable
fun PreviewCodeEditor() {
    val textFieldState = TextFieldState("""
        fun a() {
            var l = 1
        }
        println("hihi")
    """.trimIndent())
    ScriptEditor(textFieldState = textFieldState)
}