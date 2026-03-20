package com.example.scripteditor.core.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

fun <T> Flow<T>.shareInSequential(scope: CoroutineScope): SharedFlow<ValueOrPlug<T>> {
    val sharedFlow = MutableSharedFlow<ValueOrPlug<T>>(0)
    this.onEach { sharedFlow.emitWithPlug(it) }.launchIn(scope)
    return sharedFlow
}

fun <T, R> Flow<T>.channelFlowToSequential(block: Flow<ValueOrPlug<T>>.() -> Flow<ValueOrPlug<R>>): Flow<R> = this
    .transform {
        emit(ValueOrPlug.Value(it))
        emit(ValueOrPlug.Plug())
    }
    .block()
    .map{ it }
    .dropPlugs()