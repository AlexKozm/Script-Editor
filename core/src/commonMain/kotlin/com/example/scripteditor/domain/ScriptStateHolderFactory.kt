package com.example.scripteditor.domain

import kotlinx.coroutines.CoroutineScope

fun interface ScriptStateHolderFactory {
    fun create(scope: CoroutineScope): ScriptStateHolder
}