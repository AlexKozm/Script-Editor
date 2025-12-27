package com.example.scripteditor.domain

import com.example.scripteditor.data.FilesRepository
import com.example.scripteditor.data.FilesRepositoryImpl

class SaveFileUseCase(
    private val filesRepository: FilesRepository = FilesRepositoryImpl(),
) {
    suspend operator fun invoke(path: String, data: String): Result<Unit> = filesRepository.save(
        path = path,
        data = data
    )
}