package com.example.scripteditor.data

import com.example.scripteditor.core.ExecutionEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream

class ScriptExecutionRepositoryImpl(
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
): ScriptExecutionRepository {
    override fun run(
        command: String,
        arguments: List<String>,
    ) = callbackFlow {
        val process: Process = try {
            ProcessBuilder(command, *arguments.toTypedArray()).redirectErrorStream(false).start()
        } catch (e: IOException) {
            send(ExecutionEvent.SystemError("Failed to start process: ${e.localizedMessage}"))
            close()
            return@callbackFlow
        }

        val outJob = launch { processStdOut(process) }
        val errJob = launch { processStdErr(process) }

        val exitCodeJob = launch {
            try {
                val exitCode = process.waitFor()
                listOf(outJob, errJob).joinAll()
                send(ExecutionEvent.Finished(exitCode))
            } finally {
                close()
            }
        }

        awaitClose {
            if (process.isAlive) process.destroy()
            listOf(exitCodeJob, outJob, errJob).forEach { it.cancel() }
        }
    }.flowOn(ioDispatcher)
        .onEach { println("ScriptExecution: $it") } // TODO: replace with Logging

    private suspend fun ProducerScope<ExecutionEvent>.processStdErr(process: Process) {
        try {
            readAndSend(process.errorStream) { ExecutionEvent.StdErr(it) }
        } catch (e: IOException) {
            send(ExecutionEvent.SystemError("Error stream failure: ${e.message}"))
        }
    }

    private suspend fun ProducerScope<ExecutionEvent>.processStdOut(process: Process) {
        try {
            readAndSend(process.inputStream) { ExecutionEvent.StdOut(it) }
        } catch (e: IOException) {
            send(ExecutionEvent.SystemError("Read error: ${e.message}"))
        }
    }

    private suspend fun ProducerScope<ExecutionEvent>.readAndSend(
        stream: InputStream,
        block: (String) -> ExecutionEvent
    ) {
        stream.bufferedReader().use { reader ->
            reader.lineSequence().forEach { str ->
                send(block(str))
            }
        }
    }
}