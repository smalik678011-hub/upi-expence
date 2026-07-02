package com.example.domain.parser

import com.example.domain.model.NotificationData
import com.example.domain.model.ParseResult

interface ParserEngine {
    /**
     * Coordinates the processing of a raw notification.
     * Checks duplicates, looks up registered parsers, performs parsing, and verifies outcomes.
     */
    fun parseNotification(notification: NotificationData): ParseResult
}
