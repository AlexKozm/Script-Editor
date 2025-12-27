package com.example.scripteditor.data

interface FilesRepository {
    suspend fun save(path: String, data: String): Result<Unit>
    suspend fun load(path: String): Result<String>
}