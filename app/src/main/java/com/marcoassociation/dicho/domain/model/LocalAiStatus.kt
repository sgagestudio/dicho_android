package com.marcoassociation.dicho.domain.model

data class LocalAiStatus(
    val supportsLocalAi: Boolean,
    val isModelAvailable: Boolean,
    val canDownloadModel: Boolean,
    val hasInternet: Boolean
)
