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
    fillContinueSuspender: suspend (List<T>, ReceiveChannel<Unit>) -> Unit = { list, receiver ->
        if (list.size > 1) receiver.receive()
    },
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
                    println("Collecting $item; mutableList: $mutableList")
                    fillBatch(mutableList, item)
                    mutableList.toList()
                }
                emitSignalChannel.send(Unit)
                fillContinueSuspender(list, collectSignalChannel)
                yield()
            }
        }
        emitSignalChannel.consumeEach {
            val (left, consumed) = mutex.withLock {
                if (mutableList.isNotEmpty()) {
                    println("Collecting Batch $mutableList")
                    val consumed = collectBatch(mutableList)
                    val left = mutableList.toList()
                    left to consumed
                } else emptyList<T>() to emptyList()
            }
            if (consumed.isNotEmpty()) send(consumed)
            collectSignalChannel.send(Unit)
            yield()
        }
    }
}

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

fun interface BatchCollector <T> {
    companion object {}
    operator fun invoke(mutableList: MutableList<T>): List<T>
}

fun <T> BatchCollector.Companion.getAndClear() = BatchCollector<T> { mutableList ->
    mutableList.toList().also { mutableList.clear() }
}



