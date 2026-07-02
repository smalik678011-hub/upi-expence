package com.example.domain.model

data class ParsedTransaction(
    val amount: Double,
    val currency: String,
    val direction: TransactionDirection,
    val status: TransactionStatus,
    val counterpartyName: String,
    val transactionRef: String?,
    val timestamp: Long,
    val sourceApp: NotificationSource,
    val rawNotification: String,
    val confidence: Float
)
