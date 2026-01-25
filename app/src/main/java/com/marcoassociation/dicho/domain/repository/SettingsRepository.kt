package com.marcoassociation.dicho.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val useLocalAi: StateFlow<Boolean>

    suspend fun setUseLocalAi(enabled: Boolean)
}
