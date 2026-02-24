package com.example.scripteditor.core.flow.batch


import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * When an emitter works faster than a collector,
 * results produced by the emitter are stored in a list
 * which allows the collector to get a batch of results when collector is ready.
 *
 * A collection provided by lambdas of [collectBatch] and [fillBatch] is synchronized.
 * The emitter and the collector could run in different threads.
 */
fun <T> Flow<T>.batched(
    emitterContext: CoroutineContext = EmptyCoroutineContext,
    fillBatch: BatchFiller<T> = BatchFiller.sequential(),
    suspendFiller: suspend List<T>.() -> Boolean = { size > 1000 },
    collectBatch: BatchCollector<T> = BatchCollector.getAndClear()
): Flow<List<T>> = channelFlow<List<T>> {
    val mutex = Mutex()
    val mutableList = mutableListOf<T>()
    val collectSignalChannel = Channel<Unit>(Channel.CONFLATED)
    val emitSignalChannel = Channel<Unit>(Channel.CONFLATED)
    coroutineScope {
        launch(emitterContext) {
            collect { item ->
                val list = mutex.withLock {
                    fillBatch(mutableList, item)
                    mutableList.toList()
                }
                emitSignalChannel.trySend(Unit) // not send because channel is conflated
                if (suspendFiller(list)) collectSignalChannel.receive()
                yield()
            }
        }
        emitSignalChannel.consumeEach {
            val consumed = mutex.withLock {
                if (mutableList.isNotEmpty()) { collectBatch(mutableList) }
                else emptyList()
            }
            if (consumed.isNotEmpty()) send(consumed)
            collectSignalChannel.send(Unit)
            yield()
        }
    }
}

/**
 * Logic of filling mutableList on each item
 */
fun interface BatchFiller <T> {
    companion object {}
    operator fun invoke(mutableList: MutableList<T>, item: T)
}

fun <T> BatchFiller.Companion.sequential() = BatchFiller<T> { mutableList, item ->
    mutableList.add(item)
}

/**
 * If buffer stores more values than [maxSize] then the oldest are droped
 */
fun <T> BatchFiller.Companion.sequentialWithDropping(maxSize: Long) = BatchFiller<T> { mutableList, item ->
    mutableList.add(item)
    if (mutableList.size > maxSize) {
        mutableList.removeFirst()
    }
}

/**
 * Logic of getting values from a mutableList
 */
fun interface BatchCollector <T> {
    companion object {}

    /**
     * Returns a list that will be a next item in the outgoing flow
     */
    operator fun invoke(mutableList: MutableList<T>): List<T>
}

fun <T> BatchCollector.Companion.getAndClear() = BatchCollector<T> { mutableList ->
    mutableList.toList().also { mutableList.clear() }
}



