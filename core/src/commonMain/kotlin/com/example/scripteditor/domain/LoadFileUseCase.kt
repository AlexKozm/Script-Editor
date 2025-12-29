package com.example.scripteditor.domain

import com.example.scripteditor.data.FilesRepository

class LoadFileUseCase (
    private val filesRepository: FilesRepository = FilesRepository(),
) {
    suspend operator fun invoke(path: String): Result<String> = filesRepository.load(
        path = path,
    )
}