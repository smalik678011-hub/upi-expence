package com.example.domain.parser

import com.example.domain.model.NotificationSource

interface ParserFactory {
    /**
     * Creates or returns a notification parser for the given notification source.
     */
    fun createParser(source: NotificationSource): NotificationParser?
}
