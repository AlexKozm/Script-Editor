package com.example.scripteditor.data

import com.example.scripteditor.core.models.ExecutionEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.handleCoroutineException
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.selects.selectUnbiased
import kotlinx.coroutines.selects.whileSelect
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.cancellation.CancellationException
import kotlin.sequences.forEach

class ScriptExecutionSequentialRepositoryImpl(
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
): ScriptExecutionRepository {
    override fun run(
        command: String,
        arguments: List<String>,
    ) = flow {
        val process: Process = try {
            ProcessBuilder(command, *arguments.toTypedArray()).redirectErrorStream(false).start()
        } catch (e: IOException) {
            emit(ExecutionEvent.SystemError("Failed to start process: ${e.message}"))
            return@flow
        }

        val dataChannel = Channel<ExecutionEvent>()
        val ackChannel = Channel<Unit>()

        try {
            coroutineScope {
                val streamJobs = listOf(
                    launch(CoroutineName("inputStream coroutine")) {
                        dataChannel.processStdOut(process, ackChannel)
                    },
                    launch(CoroutineName("errorStream coroutine")) {
                        dataChannel.processStdErr(process, ackChannel)
                    }
                )

                launch(CoroutineName("process.waitFor")) {
                    val exitCode = process.waitFor()
                    streamJobs.joinAll()
                    ackChannel.close()
                    dataChannel.send(ExecutionEvent.Finished(exitCode))
                    dataChannel.close()
                }
                launch(CoroutineName("destroyer")) {
                    try { awaitCancellation() } finally { process.destroy() }
                }

                for (event in dataChannel) {
                    emit(event)
                    if (event !is ExecutionEvent.Finished)
                        ackChannel.send(Unit)
                }
                cancel()
            }
        } catch (e: CancellationException) {
            println("HERE: $e")
        }
    }
        .flowOn(ioDispatcher)
//        .onEach { println("ScriptExecution: $it") } // TODO: replace with Logging

    private suspend fun SendChannel<ExecutionEvent>.processStdErr(
        process: Process,
        ackChannel: ReceiveChannel<Unit>
    ) {
        try {
            readAndSend(process.errorStream, ackChannel) { ExecutionEvent.StdErr(it) }
        } catch (e: IOException) {
            send(ExecutionEvent.SystemError("Error stream failure: ${e.message}"))
        }
    }

    private suspend fun SendChannel<ExecutionEvent>.processStdOut(
        process: Process,
        ackChannel: ReceiveChannel<Unit>
    ) {
        try {
            readAndSend(process.inputStream, ackChannel) { ExecutionEvent.StdOut(it) }
        } catch (e: IOException) {
            send(ExecutionEvent.SystemError("Read error: ${e.message}"))
        }
    }

    private suspend fun SendChannel<ExecutionEvent>.readAndSend(
        stream: InputStream,
        ackChannel: ReceiveChannel<Unit>,
        block: (String) -> ExecutionEvent
    ) {
        stream.bufferedReader().use { reader ->
            reader.lineSequence().forEach { str ->
                send(block(str))
                ackChannel.receive()
            }
        }
    }
}

//actual fun ScriptExecutionSequentialRepository(): ScriptExecutionRepository = ScriptExecutionRepositoryImpl()