package com.marcoassociation.dicho.data.repository

import com.marcoassociation.dicho.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor() : SettingsRepository {
    private val useLocalAiFlow = MutableStateFlow(true)

    override val useLocalAi: StateFlow<Boolean> = useLocalAiFlow

    override suspend fun setUseLocalAi(enabled: Boolean) {
        useLocalAiFlow.value = enabled
    }
}
