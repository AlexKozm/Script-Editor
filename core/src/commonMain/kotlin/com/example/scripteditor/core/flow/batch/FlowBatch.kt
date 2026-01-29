package com.example.scripteditor.core.flow.batch


import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
    collectBatch: BatchCollector<T> = BatchCollector.getAndClear()
): Flow<List<T>> {
    val mutex = Mutex()
    val mutableList = mutableListOf<T>()
//    return flow<List<T>> {
//        coroutineScope {
//            val job = launch {
//                collect { item ->
//                    mutex.withLock {
//                        fillBatch(mutableList, item)
//                    }
//                }
//            }
//            launch {
//                while (true) {
//                    mutex.withLock {
//                        collectBatch(mutableList)
//                        if (mutableList.isNotEmpty()) {
//                            emit(mutableList.toList())
//                        }
//                    }
//                    yield()
//                }
//                while (!job.isCompleted) {
//                    mutex.withLock {
//                        collectBatch(mutableList)
//                        emit(mutableList.toList())
//                    }
//                }
//                mutex.withLock {
//                    while (mutableList.isNotEmpty()) {
//                        collectBatch(mutableList)
//                        emit(mutableList.toList())
//                    }
//                }
//            }
//        }
//    }

    return this
        .onEach { item ->
            mutex.withLock {
                fillBatch(mutableList, item)
            }
        }
        .flowOn(emitterContext)
        .conflate()
        .buffer()
        .map {
            mutex.withLock {
                collectBatch(mutableList)
            }
        }
}

fun interface BatchFiller <T> {
    companion object {}
    operator fun invoke(mutableList: MutableList<T>, item: T)
}

fun <T> BatchFiller.Companion.sequential() = BatchFiller<T> { mutableList, item -> mutableList.add(item) }
fun <T> BatchFiller.Companion.sequential(maxSize: Long) = BatchFiller<T> { mutableList, item ->
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



