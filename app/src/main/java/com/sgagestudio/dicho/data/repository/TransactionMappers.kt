package com.sgagestudio.dicho.data.repository

import com.sgagestudio.dicho.data.local.db.TransactionEntity
import com.sgagestudio.dicho.domain.model.Transaction

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    rawText = rawText,
    concept = concept,
    amount = amount,
    currency = currency,
    expenseDate = expenseDate,
    recordDate = recordDate,
    category = category,
    status = status,
    processingSource = processingSource,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    rawText = rawText,
    concept = concept,
    amount = amount,
    currency = currency,
    expenseDate = expenseDate,
    recordDate = recordDate,
    category = category,
    status = status,
    processingSource = processingSource,
)
