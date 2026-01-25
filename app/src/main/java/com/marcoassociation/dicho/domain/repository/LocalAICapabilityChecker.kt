package com.marcoassociation.dicho.domain.repository

interface LocalAICapabilityChecker {
    suspend fun isHardwareSupported(): Boolean

    suspend fun isModelDownloaded(): Boolean

    suspend fun triggerModelDownload()
}
