package com.example.scripteditor.domain

import com.example.scripteditor.data.ScriptExecutionRepository
import com.example.scripteditor.data.ScriptExecutionSequentialRepository
import kotlinx.coroutines.CoroutineScope

class ScriptStateHolderFactoryImpl(
    private val scriptExecutionRepository: ScriptExecutionRepository = ScriptExecutionRepository()
) : ScriptStateHolderFactory {
    override fun create(scope: CoroutineScope): ScriptStateHolder = ScriptStateHolderImpl2(
        scriptExecutionRepository = scriptExecutionRepository,
        scope = scope,
    )
}