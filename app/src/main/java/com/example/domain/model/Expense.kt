package com.example.domain.model

import java.util.Date

data class Expense(
    val id: String,
    val amount: Double,
    val merchantName: String,
    val uType: String, // "DEBIT" or "CREDIT"
    val date: Date,
    val transactionRef: String?, // UPI transaction ID or ref ID
    val accountOrBank: String?, // Bank or wallet abbreviation
    val rawSmsBody: String?,
    val category: String = "Uncategorized",
    val status: String = "SUCCESS"
)
