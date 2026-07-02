package com.example.data.parser

import com.example.domain.model.NotificationData
import com.example.domain.model.NotificationSource
import com.example.domain.model.ParseResult
import com.example.domain.parser.DuplicateDetector
import com.example.domain.parser.ParserEngine
import com.example.domain.parser.ParserFactory
import com.example.domain.parser.ParserLogger
import com.example.domain.parser.ParserRegistry

class ParserEngineImpl(
    private val registry: ParserRegistry,
    private val factory: ParserFactory,
    private val duplicateDetector: DuplicateDetector,
    private val logger: ParserLogger
) : ParserEngine {

    init {
        // Register the standard supported UPI parsers upon initialization
        registerDefaultParsers()
    }

    private fun registerDefaultParsers() {
        listOf(
            NotificationSource.GOOGLE_PAY,
            NotificationSource.PHONEPE,
            NotificationSource.PAYTM,
            NotificationSource.NAVI
        ).forEach { source ->
            factory.createParser(source)?.let { parser ->
                registry.registerParser(parser)
                logger.debug("ParserEngine initialized and registered default parser for: ${source.displayName}")
            }
        }
    }

    override fun parseNotification(notification: NotificationData): ParseResult {
        val packageName = notification.packageName
        logger.debug("Received notification for processing from source app: $packageName")

        // 1. Pre-parse duplicate detection (e.g. duplicate notification ID or text hash)
        if (duplicateDetector.isDuplicate(notification)) {
            logger.info("Duplicate raw notification detected from $packageName. Ignoring.")
            return ParseResult.Ignored("Duplicate raw notification")
        }

        // 2. Find parser matching package
        val parser = registry.getParserForPackage(packageName)
        if (parser == null) {
            logger.debug("No registered parser available for package: $packageName")
            return ParseResult.Ignored("No parser registered for this source app")
        }

        // 3. Register raw notification as processed to prevent immediate re-processing
        duplicateDetector.registerProcessed(notification)

        // 4. Parse notification text
        val parseResult = parser.parse(notification)

        // 5. Post-parse duplicate detection (e.g. identical amount/recipient/timestamp within proximity)
        if (parseResult is ParseResult.Success) {
            val transaction = parseResult.transaction
            if (duplicateDetector.isDuplicateParsed(transaction)) {
                logger.info("Duplicate parsed transaction detected: Amount=${transaction.amount}, Ref=${transaction.transactionRef}. Ignoring.")
                return ParseResult.Ignored("Duplicate parsed transaction")
            }
            // Register parsed transaction to prevent future duplicates of this event
            duplicateDetector.registerProcessedParsed(transaction)
            logger.info("Successfully parsed and validated transaction: Rs.${transaction.amount} with ${transaction.counterpartyName} via ${transaction.sourceApp.displayName}")
        } else {
            when (parseResult) {
                is ParseResult.Ignored -> logger.debug("Notification ignored during parsing: ${parseResult.reason}")
                is ParseResult.Invalid -> logger.warn("Notification invalid for transaction parsing: ${parseResult.reason}")
                is ParseResult.Failed -> logger.error("Parsing failed: ${parseResult.reason}", parseResult.error)
                else -> {}
            }
        }

        return parseResult
    }
}
