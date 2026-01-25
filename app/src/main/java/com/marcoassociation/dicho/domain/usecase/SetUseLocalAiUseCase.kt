package com.marcoassociation.dicho.domain.usecase

import com.marcoassociation.dicho.domain.repository.SettingsRepository
import javax.inject.Inject

class SetUseLocalAiUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) {
        settingsRepository.setUseLocalAi(enabled)
    }
}
