package com.example.data.parser

import com.example.domain.model.Expense
import com.example.domain.model.ParsedTransaction
import com.example.domain.model.TransactionDirection
import com.example.domain.parser.TransactionMapper
import java.util.Date
import java.util.UUID

class TransactionMapperImpl : TransactionMapper {

    override fun toExpense(parsedTransaction: ParsedTransaction): Expense {
        val expenseId = parsedTransaction.transactionRef ?: UUID.randomUUID().toString()
        val uTypeStr = when (parsedTransaction.direction) {
            TransactionDirection.SENT -> "DEBIT"
            TransactionDirection.RECEIVED -> "CREDIT"
            else -> "DEBIT" // Default fallback
        }

        return Expense(
            id = expenseId,
            amount = parsedTransaction.amount,
            merchantName = parsedTransaction.counterpartyName,
            uType = uTypeStr,
            date = Date(parsedTransaction.timestamp),
            transactionRef = parsedTransaction.transactionRef,
            accountOrBank = parsedTransaction.sourceApp.displayName,
            rawSmsBody = parsedTransaction.rawNotification,
            category = "Uncategorized",
            status = parsedTransaction.status.name
        )
    }
}
