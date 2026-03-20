package com.example.scripteditor.core.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalTypeInference::class)
fun <T> sequentialChannelFlow1(
    @BuilderInference
    block: suspend SequentialChannelFlowCollector<T>.() -> Unit,
): Flow<T> = channelFlow {
    SequentialChannelFlowCollectorImpl1(this, this).block()
}
    .buffer(0)
    .filterIsInstance<ValueOrPlug.Value<*>>()
    .map {
        @Suppress("UNCHECKED_CAST")
        it.value as T
    }

private class SequentialChannelFlowCollectorImpl1<T>(
    coroutineScope: CoroutineScope,
    private val producerScope: ProducerScope<ValueOrPlug<T>>
) : SequentialChannelFlowCollector<T>, CoroutineScope by coroutineScope {
    override suspend fun emit(value: T) {
        producerScope.sendWithPlug(value)
    }
}

sealed interface ValueOrPlug <T> {
    /**
     * Is used to fill channel buffer, so [SequentialChannelFlowCollector.emit] waits until
     * the actual [Value] would not only be collected, but also processed
     */
    class Plug<T>: ValueOrPlug<T>
    data class Value<T>(val value: T): ValueOrPlug<T>
}

suspend fun <T> FlowCollector<ValueOrPlug<T>>.emitWithPlug(value: T) {
    emit(ValueOrPlug.Value(value))
    emit(ValueOrPlug.Plug())
}

suspend fun <T> ProducerScope<ValueOrPlug<T>>.sendWithPlug(value: T) {
    send(ValueOrPlug.Value(value))
    send(ValueOrPlug.Plug())
}

fun <T> Flow<ValueOrPlug<T>>.dropPlugs(): Flow<T> = this
    .filterIsInstance<ValueOrPlug.Value<*>>()
    .map {
        @Suppress("UNCHECKED_CAST")
        it.value as T
    }
