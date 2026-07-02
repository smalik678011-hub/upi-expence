package com.example.data.parser

import com.example.core.log.Logger
import com.example.domain.parser.ParserLogger

class ParserLoggerImpl(private val logger: Logger) : ParserLogger {
    private val tag = "UPIParserEngine"

    override fun debug(message: String) {
        logger.d(tag, message)
        com.example.core.log.InMemoryLogStore.addParserLog("DEBUG: $message")
    }

    override fun info(message: String) {
        logger.i(tag, message)
        com.example.core.log.InMemoryLogStore.addParserLog("INFO: $message")
    }

    override fun warn(message: String, throwable: Throwable?) {
        logger.w(tag, message, throwable)
        com.example.core.log.InMemoryLogStore.addParserLog("WARN: $message${throwable?.let { " - ${it.message}" } ?: ""}")
    }

    override fun error(message: String, throwable: Throwable?) {
        logger.e(tag, message, throwable)
        com.example.core.log.InMemoryLogStore.addParserLog("ERROR: $message${throwable?.let { " - ${it.message}" } ?: ""}")
    }
}
