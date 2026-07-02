package com.example.domain.parser

import com.example.domain.model.ParsedTransaction

interface TransactionValidator {
    /**
     * Inspects the raw or normalized text to determine if it is a promotional, promotional offer,
     * cashback, loan ad, kyc reminder, bill reminder, security alert, or other non-transaction announcement.
     */
    fun isPromotionalOrNonTransaction(text: String): Boolean

    /**
     * Validates if the parsed transaction has correct, expected properties (e.g., amount > 0, valid currency).
     */
    fun validateParsed(transaction: ParsedTransaction): Boolean
}
