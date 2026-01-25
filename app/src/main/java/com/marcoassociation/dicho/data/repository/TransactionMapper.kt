package com.marcoassociation.dicho.data.repository

import com.marcoassociation.dicho.data.local.ProcessingSourceEntity
import com.marcoassociation.dicho.data.local.TransactionEntity
import com.marcoassociation.dicho.data.local.TransactionStatusEntity
import com.marcoassociation.dicho.domain.model.ProcessingSource
import com.marcoassociation.dicho.domain.model.Transaction
import com.marcoassociation.dicho.domain.model.TransactionStatus

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    rawText = rawText,
    concept = concept,
    amount = amount,
    currency = currency,
    expenseDate = expenseDate,
    recordDate = recordDate,
    category = category,
    status = status.toDomain(),
    processingSource = processingSource.toDomain()
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
    status = status.toEntity(),
    processingSource = processingSource.toEntity()
)

fun TransactionStatusEntity.toDomain(): TransactionStatus = TransactionStatus.valueOf(name)

fun TransactionStatus.toEntity(): TransactionStatusEntity = TransactionStatusEntity.valueOf(name)

fun ProcessingSourceEntity.toDomain(): ProcessingSource = ProcessingSource.valueOf(name)

fun ProcessingSource.toEntity(): ProcessingSourceEntity = ProcessingSourceEntity.valueOf(name)
