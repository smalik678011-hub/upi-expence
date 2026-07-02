package com.example.domain.parser

interface NotificationNormalizer {
    /**
     * Cleans and normalizes notification text.
     * Processes whitespace, currency symbols, unicode characters, line breaks,
     * invisible characters, and mixed Hindi/English text.
     */
    fun normalize(text: String): String
}
