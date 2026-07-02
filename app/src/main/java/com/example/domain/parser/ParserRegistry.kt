package com.example.domain.parser

interface ParserRegistry {
    /**
     * Registers a notification parser to the active registry.
     */
    fun registerParser(parser: NotificationParser)

    /**
     * Removes a notification parser from the active registry.
     */
    fun unregisterParser(parser: NotificationParser)

    /**
     * Searches for a registered parser that can handle notifications from the given package.
     */
    fun getParserForPackage(packageName: String): NotificationParser?

    /**
     * Returns a list of all currently registered parsers.
     */
    fun getAllParsers(): List<NotificationParser>
}
