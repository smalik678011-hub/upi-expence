package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        androidx.room.Index(value = ["dateLong"]),
        androidx.room.Index(value = ["merchantName"]),
        androidx.room.Index(value = ["transactionRef"]),
        androidx.room.Index(value = ["uType"]),
        androidx.room.Index(value = ["category"]),
        androidx.room.Index(value = ["status"])
    ]
)
data class ExpenseDbEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val merchantName: String,
    val uType: String,
    val dateLong: Long,
    val transactionRef: String?,
    val accountOrBank: String?,
    val rawSmsBody: String?,
    val category: String,
    val status: String = "SUCCESS"
)
