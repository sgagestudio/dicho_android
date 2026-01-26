package com.sgagestudio.dicho.domain.ai

interface LocalAICapabilityChecker {
    suspend fun isLocalAiSupported(): Boolean
    suspend fun isLocalModelDownloaded(): Boolean
}
