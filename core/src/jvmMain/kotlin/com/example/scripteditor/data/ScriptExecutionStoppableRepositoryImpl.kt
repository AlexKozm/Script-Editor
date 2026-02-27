package com.example.scripteditor.data

import com.example.scripteditor.core.models.ExecutionEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import java.io.IOException

open class ScriptExecutionStoppableRepositoryImpl(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
){
    protected suspend fun ProducerScope<ExecutionEvent>.processStarter(
        command: String,
        arguments: List<String>,
    ): Process = try {
        withContext(ioDispatcher) { // start is blocking
            ProcessBuilder(command, *arguments.toTypedArray()).redirectErrorStream(false).start()
        }
    } catch (e: IOException) {
        send(ExecutionEvent.SystemError("Failed to start process: ${e.message}"))
        close()
        throw e
    }


    protected open suspend fun ProducerScope<ExecutionEvent>.awaitClosing(process: Process) {
        awaitClose {
            if (process.isAlive) process.destroy()
        }
    }

    fun getExecutableFlow(
        command: String,
        arguments: List<String>,
    ): Flow<ExecutionEvent> = channelFlow {
        val process = processStarter(command, arguments)
        // ...
        awaitClosing(process)
    }
}