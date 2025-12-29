package com.example.scripteditor.domain

import com.example.scripteditor.data.FilesRepository

class SaveFileUseCase(
    private val filesRepository: FilesRepository = FilesRepository(),
) {
    suspend operator fun invoke(path: String, data: String): Result<Unit> = filesRepository.save(
        path = path,
        data = data
    )
}