package com.sgagestudio.dicho.domain.model

data class ReceiptBatch(
    val id: Long,
    val createdAt: Long,
    val status: ReceiptBatchStatus,
)
