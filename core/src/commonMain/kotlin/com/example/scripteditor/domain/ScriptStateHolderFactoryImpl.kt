package com.example.scripteditor.domain

import com.example.scripteditor.data.ScriptExecutionRepository
import com.example.scripteditor.data.ScriptExecutionSequentialRepository
import com.example.scripteditor.data.ScriptExecutionStoppableRepository
import kotlinx.coroutines.CoroutineScope

class ScriptStateHolderFactoryImpl(
    private val scriptExecutionRepository: ScriptExecutionStoppableRepository = ScriptExecutionSequentialRepository()
) : ScriptStateHolderFactory {
    override fun create(scope: CoroutineScope): ScriptStateHolder = ScriptStateHolderImpl(
        scriptExecutionRepository = scriptExecutionRepository,
        scope = scope,
    )
}