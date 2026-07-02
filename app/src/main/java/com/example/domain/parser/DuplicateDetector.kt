package com.example.domain.parser

import com.example.domain.model.NotificationData
import com.example.domain.model.ParsedTransaction

interface DuplicateDetector {
    /**
     * Checks if the incoming raw notification is a duplicate based on unique ID, hash, or details.
     */
    fun isDuplicate(notification: NotificationData): Boolean

    /**
     * Checks if the parsed transaction is a duplicate based on fields like amount, counterparty,
     * source app, and timestamp proximity.
     */
    fun isDuplicateParsed(transaction: ParsedTransaction): Boolean

    /**
     * Marks a raw notification as processed.
     */
    fun registerProcessed(notification: NotificationData)

    /**
     * Marks a parsed transaction as processed.
     */
    fun registerProcessedParsed(transaction: ParsedTransaction)

    /**
     * Resets or clears cache (useful for testing or memory management).
     */
    fun clearCache()
}
