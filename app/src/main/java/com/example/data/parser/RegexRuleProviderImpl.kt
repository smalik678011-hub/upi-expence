package com.example.data.parser

import com.example.domain.model.NotificationSource
import com.example.domain.parser.RegexRuleProvider

class RegexRuleProviderImpl : RegexRuleProvider {

    // Cache compiled regex patterns to avoid performance overhead
    private val generalAmountPattern = Regex("Rs\\s?([0-9,]+\\.?[0-9]*)")
    private val generalRefPattern = Regex("(?:UPI Ref|UTR|Txn ID|Ref|Ref No)[:\\s]+([0-9A-Za-z]{12,})", RegexOption.IGNORE_CASE)

    // Google Pay specific patterns
    private val gpayAmountPatterns = listOf(generalAmountPattern)
    private val gpayRefPatterns = listOf(
        generalRefPattern,
        Regex("UPI Ref\\s*([0-9]{12})", RegexOption.IGNORE_CASE),
        Regex("Ref\\s*([0-9]{12})", RegexOption.IGNORE_CASE)
    )
    private val gpayCounterpartyPatterns = listOf(
        Regex("^([^.]+?)\\s+(?:ko|को)\\s+Rs\\s*[0-9,.]+", RegexOption.IGNORE_CASE),
        Regex("^([^.]+?)\\s+(?:paid you|sent you|transferred to you)\\s+Rs\\s*[0-9,.]+", RegexOption.IGNORE_CASE),
        Regex("(?:payment of|paid)\\s+Rs\\s*[0-9,.]+\\s+to\\s+([^.]+?)(?:\\s+(?:is|Ref|UTR|Txn|successful|done|failed|pending)|\\.|$)", RegexOption.IGNORE_CASE),
        Regex("paid you Rs\\s*[0-9,.]+\\s*(?:from|using|with)?\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE),
        Regex("sent you Rs\\s*[0-9,.]+\\s*(?:from|using|with)?\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE),
        Regex("paid Rs\\s*[0-9,.]+\\s*to\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE),
        Regex("sent Rs\\s*[0-9,.]+\\s*to\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE),
        Regex("Rs\\s*[0-9,.]+\\s+(?:received\\s+)?from\\s+([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE),
        Regex("received Rs\\s*[0-9,.]+\\s*from\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE)
    )

    // PhonePe specific patterns
    private val phonePeAmountPatterns = listOf(generalAmountPattern)
    private val phonePeRefPatterns = listOf(
        generalRefPattern,
        Regex("Txn\\s*([A-Za-z0-9]+)", RegexOption.IGNORE_CASE)
    )
    private val phonePeCounterpartyPatterns = listOf(
        Regex("(?:transaction of|paid)\\s+Rs\\s*[0-9,.]+\\s+to\\s+([^.]+?)(?:\\s+(?:failed|Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE),
        Regex("Paid Rs\\s*[0-9,.]+\\s*to\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE),
        Regex("You have sent Rs\\s*[0-9,.]+\\s*to\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE),
        Regex("Received Rs\\s*[0-9,.]+\\s*from\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE),
        Regex("credited to.*from\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE)
    )

    // Paytm specific patterns
    private val paytmAmountPatterns = listOf(generalAmountPattern)
    private val paytmRefPatterns = listOf(
        generalRefPattern,
        Regex("Ref\\s*([0-9]{12})", RegexOption.IGNORE_CASE)
    )
    private val paytmCounterpartyPatterns = listOf(
        Regex("Paid Rs\\s*[0-9,.]+\\s*to\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE),
        Regex("Sent Rs\\s*[0-9,.]+\\s*to\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE),
        Regex("Received Rs\\s*[0-9,.]+\\s*from\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE)
    )

    // Navi specific patterns
    private val naviAmountPatterns = listOf(generalAmountPattern)
    private val naviRefPatterns = listOf(generalRefPattern)
    private val naviCounterpartyPatterns = listOf(
        Regex("Paid Rs\\s*[0-9,.]+\\s*to\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE),
        Regex("Received Rs\\s*[0-9,.]+\\s*from\\s*([^.]+?)(?:\\s+(?:Ref|UTR|Txn|successful|done)|\\.|$)", RegexOption.IGNORE_CASE)
    )

    override fun getAmountPatterns(source: NotificationSource): List<Regex> {
        return when (source) {
            NotificationSource.GOOGLE_PAY -> gpayAmountPatterns
            NotificationSource.PHONEPE -> phonePeAmountPatterns
            NotificationSource.PAYTM -> paytmAmountPatterns
            NotificationSource.NAVI -> naviAmountPatterns
            else -> listOf(generalAmountPattern)
        }
    }

    override fun getReferencePatterns(source: NotificationSource): List<Regex> {
        return when (source) {
            NotificationSource.GOOGLE_PAY -> gpayRefPatterns
            NotificationSource.PHONEPE -> phonePeRefPatterns
            NotificationSource.PAYTM -> paytmRefPatterns
            NotificationSource.NAVI -> naviRefPatterns
            else -> listOf(generalRefPattern)
        }
    }

    override fun getCounterpartyPatterns(source: NotificationSource): List<Regex> {
        return when (source) {
            NotificationSource.GOOGLE_PAY -> gpayCounterpartyPatterns
            NotificationSource.PHONEPE -> phonePeCounterpartyPatterns
            NotificationSource.PAYTM -> paytmCounterpartyPatterns
            NotificationSource.NAVI -> naviCounterpartyPatterns
            else -> emptyList()
        }
    }

    override fun getDebitPatterns(source: NotificationSource): List<Regex> {
        return listOf(
            Regex("(?:paid|sent|transferred|debited|spent|gifting)\\s", RegexOption.IGNORE_CASE)
        )
    }

    override fun getCreditPatterns(source: NotificationSource): List<Regex> {
        return listOf(
            Regex("(?:received|received from|credited|added|refunded)\\s", RegexOption.IGNORE_CASE)
        )
    }
}
