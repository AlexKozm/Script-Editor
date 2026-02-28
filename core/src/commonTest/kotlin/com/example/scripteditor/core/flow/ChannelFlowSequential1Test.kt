package com.example.scripteditor.core.flow

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChannelFlowSequential1Test : FunSpec({


    test("channelFlow should not change sequence of calls").config(coroutineTestScope = true) {
        val mutex = Mutex()
        val list1 = mutableListOf<String>()
        val list2 = mutableListOf<String>()
        suspend infix fun String.logInto(list: MutableList<String>) { mutex.withLock { list.add(this) } }

        flow {
            repeat(4) {
                "emit:    $it" logInto list1
                emit(it)
                "emitted: $it" logInto list1
            }
        }.collect {
            delay(1000)
            "collect: $it" logInto list1
        }

        sequentialChannelFlow1 {
            repeat(4) {
                "emit:    $it" logInto list2
                emit(it)
                "emitted: $it" logInto list2
            }
        }.collect {
            delay(1000)
            "collect: $it" logInto list2
        }

        list2 shouldBeEqual list1 // as the code almost the same
    }
})