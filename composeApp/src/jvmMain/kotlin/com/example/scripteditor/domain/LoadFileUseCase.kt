package com.example.scripteditor.domain

import com.example.scripteditor.data.FilesRepository
import com.example.scripteditor.data.FilesRepositoryImpl

class LoadFileUseCase (
    private val filesRepository: FilesRepository = FilesRepositoryImpl(),
) {
    suspend operator fun invoke(path: String): Result<String> = filesRepository.load(
        path = path,
    )
}