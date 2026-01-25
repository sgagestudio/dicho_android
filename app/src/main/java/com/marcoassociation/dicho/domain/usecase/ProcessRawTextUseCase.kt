package com.marcoassociation.dicho.domain.usecase

import com.marcoassociation.dicho.domain.repository.AIProcessorRepository
import javax.inject.Inject

class ProcessRawTextUseCase @Inject constructor(
    private val aiProcessorRepository: AIProcessorRepository
) {
    suspend operator fun invoke(rawText: String) {
        aiProcessorRepository.processAndStore(rawText)
    }
}
