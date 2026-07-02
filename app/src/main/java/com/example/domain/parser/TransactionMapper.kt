package com.example.domain.parser

import com.example.domain.model.Expense
import com.example.domain.model.ParsedTransaction

interface TransactionMapper {
    /**
     * Maps a cleanly parsed transaction into an Expense domain entity ready for storage.
     */
    fun toExpense(parsedTransaction: ParsedTransaction): Expense
}
