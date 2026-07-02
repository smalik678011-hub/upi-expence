package com.example.data.parser

import com.example.domain.parser.NotificationParser
import com.example.domain.parser.ParserRegistry
import java.util.concurrent.CopyOnWriteArrayList

class ParserRegistryImpl : ParserRegistry {
    private val parsers = CopyOnWriteArrayList<NotificationParser>()

    override fun registerParser(parser: NotificationParser) {
        if (!parsers.contains(parser)) {
            parsers.add(parser)
        }
    }

    override fun unregisterParser(parser: NotificationParser) {
        parsers.remove(parser)
    }

    override fun getParserForPackage(packageName: String): NotificationParser? {
        return parsers.firstOrNull { it.canParse(packageName) }
    }

    override fun getAllParsers(): List<NotificationParser> {
        return parsers.toList()
    }
}
