package com.sgagestudio.dicho.data.local

import android.os.Build
import com.sgagestudio.dicho.domain.ai.LocalAICapabilityChecker
import javax.inject.Inject

class LocalAICapabilityCheckerImpl @Inject constructor() : LocalAICapabilityChecker {
    override suspend fun isLocalAiSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }

    override suspend fun isLocalModelDownloaded(): Boolean {
        return false
    }
}
