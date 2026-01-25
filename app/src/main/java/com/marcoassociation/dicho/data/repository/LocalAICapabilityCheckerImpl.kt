package com.marcoassociation.dicho.data.repository

import com.marcoassociation.dicho.domain.repository.LocalAICapabilityChecker
import javax.inject.Inject

class LocalAICapabilityCheckerImpl @Inject constructor() : LocalAICapabilityChecker {
    override suspend fun isHardwareSupported(): Boolean {
        // TODO: Integrate with Android AICore/Gemini Nano capability checks.
        return false
    }

    override suspend fun isModelDownloaded(): Boolean {
        // TODO: Check if local on-device model has been downloaded.
        return false
    }

    override suspend fun triggerModelDownload() {
        // TODO: Trigger download for local AI model when supported.
    }
}
