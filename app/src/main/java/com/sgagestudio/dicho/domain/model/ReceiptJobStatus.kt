package com.sgagestudio.dicho.domain.model

enum class ReceiptJobStatus {
    QUEUED,
    PROCESSING,
    OCR_DONE,
    GEMINI_DONE,
    FAILED,
    CONFIRMED,
}
