package com.example.scripteditor.core.flow

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FlowTest : FunSpec({


    test("channelFlow should not change sequence of calls").config(coroutineTestScope = true) {
        val mutex = Mutex()
        val list1 = mutableListOf<String>()
        val list2 = mutableListOf<String>()
        suspend infix fun String.logInto(list: MutableList<String>) { mutex.withLock { list.add(this) } }

        flow {
            repeat(4) {
                "before emit: $it" logInto list1
                emit(it)
                "after  emit: $it" logInto list1
            }
        }
            .collect {
                delay(1000)
                "collect:     $it" logInto list1
            }

        channelFlow {
            repeat(4) {
                "before send: $it" logInto list2
                send(it)
                "after  emit: $it" logInto list1

            }
        }.buffer(capacity = 0, onBufferOverflow = BufferOverflow.SUSPEND)
            .collect {
                delay(1000)
                "collect:     $it" logInto list2
            }

        list2 shouldBeEqual list1 // as the code almost the same
    }

    test("flowOn with Dispatcher should not change sequence of calls") {
        val mutex = Mutex()
        val list1 = mutableListOf<String>()
        val list2 = mutableListOf<String>()
        suspend infix fun String.logInto(list: MutableList<String>) { mutex.withLock { list.add(this) } }

        val normalFlow = flow {
            repeat(4) {
                emit(it)
            }
        }

        normalFlow
            .onEach { "onEach:  $it" logInto list1 }
            .collect { "collect: $it" logInto list1 }

        val a = normalFlow
            .onEach { "onEach:  $it" logInto list2 }
            .flowOn(Dispatchers.IO)
            .buffer(capacity = 0, onBufferOverflow = BufferOverflow.SUSPEND)

        a.collect {
            delay(10)
            "collect: $it" logInto list2
        }

        list2 shouldBeEqual list1 // as we just changed a dispatcher, nothing more...
    }
})