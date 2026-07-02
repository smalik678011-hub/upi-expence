package com.example.domain.repository

import com.example.core.error.ResultWrapper
import com.example.domain.model.Expense
import java.io.OutputStream
import java.util.Date

interface ExportRepository {
    suspend fun getExpensesCount(startDate: Date?, endDate: Date?): ResultWrapper<Int>
    suspend fun getExpensesPaged(startDate: Date?, endDate: Date?, limit: Int, offset: Int): ResultWrapper<List<Expense>>
    suspend fun exportToCsv(outputStream: OutputStream, startDate: Date?, endDate: Date?, onProgress: (Float) -> Unit): ResultWrapper<Unit>
    suspend fun exportToPdf(outputStream: OutputStream, startDate: Date?, endDate: Date?, onProgress: (Float) -> Unit): ResultWrapper<Unit>
}
