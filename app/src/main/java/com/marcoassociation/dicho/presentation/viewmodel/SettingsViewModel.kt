package com.marcoassociation.dicho.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcoassociation.dicho.domain.repository.AIProcessorRepository
import com.marcoassociation.dicho.domain.repository.SettingsRepository
import com.marcoassociation.dicho.domain.usecase.CsvExporterUseCase
import com.marcoassociation.dicho.domain.usecase.SetUseLocalAiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val setUseLocalAiUseCase: SetUseLocalAiUseCase,
    private val aiProcessorRepository: AIProcessorRepository,
    private val csvExporterUseCase: CsvExporterUseCase
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.useLocalAi,
        aiProcessorRepository.localAiStatus
    ) { useLocalAi, localAiStatus ->
        SettingsUiState(
            useLocalAi = useLocalAi,
            localAiStatus = localAiStatus
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun toggleUseLocalAi(enabled: Boolean) {
        viewModelScope.launch {
            setUseLocalAiUseCase(enabled)
            aiProcessorRepository.refreshCapabilities()
        }
    }

    fun exportCsv(uri: android.net.Uri, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = csvExporterUseCase(uri)
            onResult(result)
        }
    }
}

data class SettingsUiState(
    val useLocalAi: Boolean = true,
    val localAiStatus: com.marcoassociation.dicho.domain.model.LocalAiStatus =
        com.marcoassociation.dicho.domain.model.LocalAiStatus(
            supportsLocalAi = false,
            isModelAvailable = false,
            canDownloadModel = false,
            hasInternet = false
        )
)
