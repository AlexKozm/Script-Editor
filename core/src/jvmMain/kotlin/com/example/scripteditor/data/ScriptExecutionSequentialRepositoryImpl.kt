package com.example.scripteditor.data

import com.example.scripteditor.core.models.ExecutionEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.cancellation.CancellationException

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

        val dataChannel = Channel<ExecutionEvent>(0)
        val ackChannel = Channel<Unit>(0)

        try {
            coroutineScope {
                val streamJobs = listOf(
                    launch(CoroutineName("inputStream coroutine")) {
                        process.stdOutTo(dataChannel, ackChannel)
                    },
                    launch(CoroutineName("errorStream coroutine")) {
                        process.stdErrTo(dataChannel, ackChannel)
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

    private suspend fun Process.stdErrTo(dataChannel: SendChannel<ExecutionEvent>, ackChannel: Channel<Unit>) {
        try {
            dataChannel.readAndSend(this.errorStream, ackChannel) { ExecutionEvent.StdErr(it) }
        } catch (e: IOException) {
            dataChannel.send(ExecutionEvent.SystemError("Error stream failure: ${e.message}"))
        }
    }

    private suspend fun Process.stdOutTo(dataChannel: SendChannel<ExecutionEvent>, ackChannel: Channel<Unit>) {
        try {
            dataChannel.readAndSend(this.inputStream, ackChannel) { ExecutionEvent.StdOut(it) }
        } catch (e: IOException) {
            dataChannel.send(ExecutionEvent.SystemError("Read error: ${e.message}"))
        }
    }

    private suspend fun SendChannel<ExecutionEvent>.readAndSend(
        stream: InputStream,
        ackChannel: Channel<Unit>,
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