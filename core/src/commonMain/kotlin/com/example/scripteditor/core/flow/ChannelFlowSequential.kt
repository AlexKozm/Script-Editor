package com.example.scripteditor.core.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.experimental.ExperimentalTypeInference

interface SequentialChannelFlowCollector<in T> : FlowCollector<T>, CoroutineScope

private class SequentialChannelFlowCollectorImpl<T>(
    coroutineScope: CoroutineScope,
    val dataChannel: Channel<T>,
    val ackChannel: Channel<Unit>,
) : SequentialChannelFlowCollector<T>, CoroutineScope by coroutineScope {
    override suspend fun emit(value: T) {
        dataChannel.send(value)
        ackChannel.receive()
    }
}

@OptIn(ExperimentalTypeInference::class)
fun <T> sequentialChannelFlow(
    @BuilderInference
    block: suspend SequentialChannelFlowCollector<T>.() -> Unit,
): Flow<T> {
    return flow {
        val dataChannel: Channel<T> = Channel(0)
        val ackChannel: Channel<Unit> = Channel(0)
        coroutineScope {
            launch {
                coroutineScope {
                    SequentialChannelFlowCollectorImpl(
                        coroutineScope = this,
                        dataChannel = dataChannel,
                        ackChannel = ackChannel
                    ).block()
                }
                ackChannel.close()
                dataChannel.close()
            }
            for(data in dataChannel) {
                emit(data)
                ackChannel.send(Unit)
            }
        }
    }
}
