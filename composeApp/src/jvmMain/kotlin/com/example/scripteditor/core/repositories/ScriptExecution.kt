package com.example.scripteditor.core.repositories

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.IOException
import java.io.InputStream

class ScriptExecution(
    val command: String,
    val arguments: List<String>,
    val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    fun run() = callbackFlow {
        val process: Process = try {
            ProcessBuilder(command, *arguments.toTypedArray()).redirectErrorStream(false).start()
        } catch (e: IOException) {
            trySend(ExecutionEvent.SystemError("Failed to start process: ${e.localizedMessage}"))
            close()
            return@callbackFlow
        }

        val outJob = launch(coroutineDispatcher) { processStdOut(process) }
        val errJob = launch(coroutineDispatcher) { processStdErr(process) }

        val exitCodeJob = launch(coroutineDispatcher) {
            try {
                val exitCode = process.waitFor()
                outJob.join()
                errJob.join()
                trySend(ExecutionEvent.Finished(exitCode))
            } finally {
                close()
            }
        }

        awaitClose {
            if (process.isAlive) process.destroy()
            exitCodeJob.cancel()
            outJob.cancel()
            errJob.cancel()
        }
    }.flowOn(coroutineDispatcher)

    private suspend fun ProducerScope<ExecutionEvent>.processStdErr(process: Process) {
        try {
            readAndSend(process.errorStream) { ExecutionEvent.StdErr(it) }
        } catch (e: IOException) {
            trySend(ExecutionEvent.SystemError("Error stream failure: ${e.localizedMessage}"))
        }
    }

    private suspend fun ProducerScope<ExecutionEvent>.processStdOut(process: Process) {
        try {
            readAndSend(process.inputStream) { ExecutionEvent.StdOut(it) }
        } catch (e: IOException) {
            trySend(ExecutionEvent.SystemError("Read error: ${e.localizedMessage}"))
        }
    }

    private suspend fun ProducerScope<ExecutionEvent>.readAndSend(
        stream: InputStream,
        block: (String) -> ExecutionEvent
    ) {
        stream.bufferedReader().use { reader ->
            reader.lineSequence().forEach { str ->
                yield()
                trySend(block(str))
            }
        }
    }
}