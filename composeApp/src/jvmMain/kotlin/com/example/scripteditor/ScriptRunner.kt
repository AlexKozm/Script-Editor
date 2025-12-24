package com.example.scripteditor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.pathString

class ScriptRunner(
    private val filePath: String = "foo.kts",
    private val command: String = "kotlinc -script"
) {

    fun saveAndRun(script: String): Flow<String> {
        println("Saving script $script")
        saveScript(script = script)
        return executeCommand()
    }

    fun saveScript(path: String = filePath, script: String) {
        val file = File(Path(path).absolute().pathString)
        file.writeText(script)
        file.setExecutable(true)
    }

    private fun executeCommand() = callbackFlow {
        val command = command.split("\\s".toRegex()) + Path(filePath).absolute().pathString
        val process = ProcessBuilder(*command.toTypedArray())
            .redirectErrorStream(true)
            .start()

        val reader = process.inputStream.bufferedReader()

        try {
            var line: String? = reader.readLine()
            while (line != null) {
                trySend(line)
                line = reader.readLine()
            }
        } catch (e: Exception) {
            close(e)
        } finally {
            process.waitFor()
            close()
        }

        awaitClose {
            if (process.isAlive) {
                process.destroy()
            }
        }
    }.flowOn(Dispatchers.IO)
}