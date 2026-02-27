package com.example.scripteditor.data

import com.example.scripteditor.core.models.ExecutionEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.io.InputStream

class ScriptExecutionSequentialRepositoryImpl(
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
): ScriptExecutionStoppableRepository {
    override fun run(
        command: String,
        arguments: List<String>,
        stopSignal: CompletableDeferred<Unit>
    ) = flow {
        val process: Process = try { withContext(ioDispatcher) { // start is blocking
            ProcessBuilder(command, *arguments.toTypedArray()).redirectErrorStream(false).start()
        } } catch (e: IOException) {
            emit(ExecutionEvent.SystemError("Failed to start process: ${e.message}"))
            return@flow
        }

        val channel = ChannelWithAck<ExecutionEvent>()
        coroutineScope {
            launch {
                launch(CoroutineName("inputStream coroutine")) { process.stdOutTo(channel) }
                launch(CoroutineName("errorStream coroutine")) { process.stdErrTo(channel) }
                launch { try { stopSignal.await() } finally { process.destroy() } }
                withContext(ioDispatcher) {
                    val exitCode = process.waitFor()
                    println("Code: $exitCode")
                    channel.close(ExecutionEvent.Finished(exitCode))
                }
                coroutineContext.cancelChildren()
            }
            channel.collect {
                event -> emit(event)
            }
        }
    }
//        .onEach { println("ScriptExecution: $it") } // TODO: replace with Logging

    private suspend fun Process.stdErrTo(channel: ChannelWithAck<ExecutionEvent>) {
        try {
            errorStream.readAndSendTo(channel) { ExecutionEvent.StdErr(it) }
        } catch (e: IOException) {
            channel.send(ExecutionEvent.SystemError("Error stream failure: ${e.message}"))
        }
    }

    private suspend fun Process.stdOutTo(channel: ChannelWithAck<ExecutionEvent>) {
        try {
            inputStream.readAndSendTo(channel) { ExecutionEvent.StdOut(it) }
        } catch (e: IOException) {
            channel.send(ExecutionEvent.SystemError("Read error: ${e.message}"))
        }
    }

    private suspend fun InputStream.readAndSendTo(
        channel: ChannelWithAck<ExecutionEvent>,
        block: (String) -> ExecutionEvent
    ) = withContext(ioDispatcher) {
        bufferedReader().use { reader ->
            reader.lineSequence().forEach { str ->
                channel.send(block(str))
            }
        }
    }
}

private class ChannelWithAck<T>(
    private val dataChannel: Channel<T> = Channel(0),
    private val ackChannel: Channel<Unit> = Channel(0),
) {
    private suspend fun ack() = ackChannel.send(Unit)
    private suspend fun waitAck() = ackChannel.receive()

    suspend fun send(data: T) {
        dataChannel.send(data)
        waitAck()
    }
    suspend fun close(lastEvent: T) {
        ackChannel.close()
        println("Sending close")
        dataChannel.send(lastEvent)
        println("Closing")
        dataChannel.close()
    }
    suspend fun collect(block: suspend (T) -> Unit) {
        println("Entering collect")
        for (event in dataChannel) {
            println("Got event")
            block(event)
            println("Emitted")
            try {
                ack()
                println("acknowledged")
            } catch (_: ClosedSendChannelException) {}
        }
        println("About to exit collect")
    }
}

actual fun ScriptExecutionSequentialRepository(): ScriptExecutionStoppableRepository = ScriptExecutionSequentialRepositoryImpl()