package com.example.domain.parser

import com.example.domain.model.NotificationData
import com.example.domain.model.ParseResult

interface NotificationParser {
    /**
     * Checks if this parser is designed for the specified package or app source.
     */
    fun canParse(packageName: String): Boolean

    /**
     * Performs clean parsing of the notification content to extract transaction data.
     */
    fun parse(notification: NotificationData): ParseResult
}
