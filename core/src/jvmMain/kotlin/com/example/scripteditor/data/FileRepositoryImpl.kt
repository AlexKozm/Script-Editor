package com.example.scripteditor.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class FilesRepositoryImpl(
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FilesRepository {
    override suspend fun save(path: String, data: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            val file = File(path)
            file.writeText(data)
            file.setExecutable(true)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    override suspend fun load(path: String): Result<String> = withContext(ioDispatcher) {
        try {
            val data = File(path).readText()
            Result.success(data)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}

actual fun FilesRepository(): FilesRepository = FilesRepositoryImpl()