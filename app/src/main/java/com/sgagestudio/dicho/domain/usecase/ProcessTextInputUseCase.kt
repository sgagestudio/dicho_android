package com.sgagestudio.dicho.domain.usecase

import com.sgagestudio.dicho.domain.model.ProcessingSource
import com.sgagestudio.dicho.domain.model.Transaction
import com.sgagestudio.dicho.domain.repository.AIProcessorRepository
import javax.inject.Inject

class ProcessTextInputUseCase @Inject constructor(
    private val aiProcessorRepository: AIProcessorRepository,
) {
    suspend fun processTextInput(
        rawText: String,
    ): Result<Pair<List<Transaction>, ProcessingSource>> {
        return aiProcessorRepository.process(rawText)
    }
}
