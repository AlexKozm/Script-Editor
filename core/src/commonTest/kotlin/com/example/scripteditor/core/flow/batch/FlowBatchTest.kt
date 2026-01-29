package com.example.scripteditor.core.flow.batch

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestScope
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList

@OptIn(ExperimentalCoroutinesApi::class)
class FlowBatchTest: FunSpec({
    test("The first value is collected immediately as it was produced").config(coroutineTestScope = true) {
        val emitter = flow {
            (1..3).forEach {
                emit(it)
                delay(1000)
            }
        }
        emitter
            .batched()
            .collect { item ->
                if (item.size == 1 && item[0] == 1) {
                    testCoroutineScheduler.currentTime shouldBe 0
                }
                delay(2001)
            }
    }

    test("Fast emitter slow collector").config(coroutineTestScope = true) {
        val emitter = flow {
            (1..5).forEach {
                emit(it)
                delay(1000)
            }
        }
        val res = emitter
            .batched()
            .map { it to testCoroutineScheduler.currentTime.toInt() }
            .onEach { delay(2001) }
            .toList()

        res shouldBe listOf(
            listOf(1) to 0,
            listOf(2, 3) to 2001,
            listOf(4, 5) to 4002,
        )
    }
})