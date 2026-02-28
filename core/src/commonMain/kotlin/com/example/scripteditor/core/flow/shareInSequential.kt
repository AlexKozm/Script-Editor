package com.example.scripteditor.core.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun <T> Flow<T>.shareInSequential(scope: CoroutineScope): SharedFlow<ValueOrPlug<T>> {
    val sharedFlow = MutableSharedFlow<ValueOrPlug<T>>()
    this.onEach { sharedFlow.emitWithPlug(it) }.launchIn(scope)
    return sharedFlow
}