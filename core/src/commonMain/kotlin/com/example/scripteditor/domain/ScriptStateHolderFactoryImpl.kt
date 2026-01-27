package com.example.scripteditor.domain

import com.example.scripteditor.data.ScriptExecutionRepository
import kotlinx.coroutines.CoroutineScope

class ScriptStateHolderFactoryImpl(
    private val scriptExecutionRepository: ScriptExecutionRepository = ScriptExecutionRepository()
) : ScriptStateHolderFactory {
    override fun create(scope: CoroutineScope): ScriptStateHolder = ScriptStateHolderImpl(
        scriptExecutionRepository = scriptExecutionRepository,
        scope = scope,
    )
}