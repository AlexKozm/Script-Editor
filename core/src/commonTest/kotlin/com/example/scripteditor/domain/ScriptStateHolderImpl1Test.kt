package com.example.scripteditor.domain

import com.example.scripteditor.core.flow.ValueOrPlug
import com.example.scripteditor.core.flow.dropPlugs
import com.example.scripteditor.core.flow.emitWithPlug
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.testCoroutineScheduler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestCoroutineScheduler

@OptIn(ExperimentalCoroutinesApi::class)
class ScriptStateHolderImpl1Test: FunSpec({

    fun f(testCoroutineScheduler: TestCoroutineScheduler) = listOf(1).asFlow().transform {
        delay(100)
        println("${testCoroutineScheduler.currentTime}:\t emitting  1")
        emit(1)
        println("${testCoroutineScheduler.currentTime}:\t emitted   1")

        delay(100)

        println("${testCoroutineScheduler.currentTime}:\t emitting  2")
        emit(2)
        println("${testCoroutineScheduler.currentTime}:\t emitted   2")

        delay(100)

        println("${testCoroutineScheduler.currentTime}:\t emitting  3")
        emit(3)
        println("${testCoroutineScheduler.currentTime}:\t emitted   3")
    }

    test("sequential custom MutableSharedFlow").config(coroutineTestScope = true) {
        val sharedFlow = MutableSharedFlow<Pair<List<CompletableDeferred<Unit>>, Int>>()
        f(testCoroutineScheduler).onEach {
            val list = List(2) { CompletableDeferred<Unit>() }
            sharedFlow.emit(list to it)
            list.awaitAll()
        }.launchIn(this)



//        val a = f.combine(sharedFlow.subscriptionCount) { item, subsN ->
//            subsN to item
//        }
//            .pairScan(null to (0 to null))
//            .onEach { (prev, new) ->
//                if (prev?.first == new.first || prev?.second != new.second) {
//                    val list = List(new.first) { CompletableDeferred<Unit>() }
//                    sharedFlow.emit(list to new.second)
//                    list.awaitAll()
//                }
//            }
//            .map { it.second }
//            .filterNotNull()


        sharedFlow
            .transform {
                emit(it.second)
                it.first[0].complete(Unit)
            }
            .onEach { println("${testCoroutineScheduler.currentTime}:\t collected $it without delay") }
            .launchIn(this)

        sharedFlow
            .transform {
                emit(it.second)
                it.first[1].complete(Unit)
            }
            .onEach {
                delay(1000)
                println("${testCoroutineScheduler.currentTime}:\t collected $it with    delay")
            }
            .launchIn(this)

        delay(100000)
        coroutineContext.cancelChildren()
    }

    test("MutableSharedFlow should be sequential").config(coroutineTestScope = true) {
        val sharedFlow = f(testCoroutineScheduler)
            .buffer(0)
            .shareIn(this, SharingStarted.Eagerly)

        sharedFlow
            .onEach { println("${testCoroutineScheduler.currentTime}:\t collected $it without delay") }
            .launchIn(this)

        sharedFlow
            .onEach {
                delay(1000)
                println("${testCoroutineScheduler.currentTime}:\t collected $it with    delay")
            }
            .launchIn(this)

        delay(100000)
        coroutineContext.cancelChildren()
    }

    test("sequentiality with plug").config(coroutineTestScope = true) {
        val sharedFlow = MutableSharedFlow<ValueOrPlug<Int>>()
            f(testCoroutineScheduler).onEach {
                sharedFlow.emitWithPlug(it)
            }.launchIn(this)

        sharedFlow
            .dropPlugs()
            .onEach { println("${testCoroutineScheduler.currentTime}:\t collected $it without delay") }
            .launchIn(this)

        sharedFlow
            .dropPlugs()
            .onEach {
                delay(1000)
                println("${testCoroutineScheduler.currentTime}:\t collected $it with    delay")
            }
            .launchIn(this)

        delay(100000)
        coroutineContext.cancelChildren()
    }
})

