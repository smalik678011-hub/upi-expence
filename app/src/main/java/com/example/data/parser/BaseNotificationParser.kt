package com.example.data.parser

import com.example.domain.model.NotificationData
import com.example.domain.model.NotificationSource
import com.example.domain.model.ParseResult
import com.example.domain.model.ParsedTransaction
import com.example.domain.model.TransactionDirection
import com.example.domain.model.TransactionStatus
import com.example.domain.parser.KeywordRuleProvider
import com.example.domain.parser.NotificationNormalizer
import com.example.domain.parser.NotificationParser
import com.example.domain.parser.RegexRuleProvider
import com.example.domain.parser.TransactionValidator

abstract class BaseNotificationParser(
    protected val source: NotificationSource,
    protected val normalizer: NotificationNormalizer,
    protected val regexRuleProvider: RegexRuleProvider,
    protected val keywordRuleProvider: KeywordRuleProvider,
    protected val validator: TransactionValidator
) : NotificationParser {

    override fun canParse(packageName: String): Boolean {
        return source.packageName == packageName
    }

    override fun parse(notification: NotificationData): ParseResult {
        try {
            val text = notification.text ?: notification.bigText ?: ""
            if (text.isBlank()) {
                return ParseResult.Invalid("Empty notification text")
            }

            // Step 1: Normalize the notification text
            val normalizedText = normalizer.normalize(text)

            // Step 2: Validate if it's promotional, spam, or alert
            if (validator.isPromotionalOrNonTransaction(normalizedText)) {
                return ParseResult.Ignored("Promotional, alert, or non-transaction notification detected")
            }

            // Step 3: Extract fields
            val amount = extractAmount(normalizedText)
            val currency = "INR" // Standard currency for Indian UPI
            val direction = extractDirection(normalizedText)
            val status = extractStatus(normalizedText)
            val counterpartyName = extractCounterparty(normalizedText, notification.title ?: "")
            val refId = extractReference(normalizedText)

            // Step 4: Calculate confidence score based on successfully parsed key elements
            val confidence = calculateConfidence(amount, direction, counterpartyName, refId)

            val transaction = ParsedTransaction(
                amount = amount,
                currency = currency,
                direction = direction,
                status = status,
                counterpartyName = counterpartyName,
                transactionRef = refId,
                timestamp = notification.postTime,
                sourceApp = source,
                rawNotification = text,
                confidence = confidence
            )

            // Step 5: Validate the parsed transaction
            if (!validator.validateParsed(transaction)) {
                return ParseResult.Invalid("Parsed transaction failed validation criteria")
            }

            return ParseResult.Success(transaction)
        } catch (e: Exception) {
            return ParseResult.Failed("Exception occurred during parsing: ${e.message}", e)
        }
    }

    protected open fun extractAmount(text: String): Double {
        val patterns = regexRuleProvider.getAmountPatterns(source)
        for (regex in patterns) {
            val matchResult = regex.find(text)
            if (matchResult != null) {
                val groupVal = matchResult.groups[1]?.value ?: continue
                val cleanedVal = groupVal.replace(",", "")
                val parsedDouble = cleanedVal.toDoubleOrNull()
                if (parsedDouble != null) {
                    return parsedDouble
                }
            }
        }
        return 0.0
    }

    protected open fun extractReference(text: String): String? {
        val patterns = regexRuleProvider.getReferencePatterns(source)
        for (regex in patterns) {
            val matchResult = regex.find(text)
            if (matchResult != null) {
                return matchResult.groups[1]?.value?.trim()
            }
        }
        return null
    }

    protected open fun extractCounterparty(text: String, title: String): String {
        val patterns = regexRuleProvider.getCounterpartyPatterns(source)
        for (regex in patterns) {
            val matchResult = regex.find(text)
            if (matchResult != null) {
                val matchedVal = matchResult.groups[1]?.value?.trim() ?: continue
                if (matchedVal.isNotBlank() && !matchedVal.equals("you", ignoreCase = true)) {
                    return cleanCounterparty(matchedVal)
                }
            }
        }
        
        // Fallback to title if title contains a clean sender/receiver name (and is not just app name)
        if (title.isNotBlank() && !title.equals(source.displayName, ignoreCase = true) && !title.contains("Pay", ignoreCase = true)) {
            return cleanCounterparty(title)
        }
        
        return "Unknown UPI Merchant"
    }

    protected open fun extractDirection(text: String): TransactionDirection {
        val lowerText = text.lowercase()
        val debitKeywords = keywordRuleProvider.getDebitKeywords(source)
        val creditKeywords = keywordRuleProvider.getCreditKeywords(source)

        for (keyword in creditKeywords) {
            if (lowerText.contains(keyword.lowercase())) {
                return TransactionDirection.RECEIVED
            }
        }
        for (keyword in debitKeywords) {
            if (lowerText.contains(keyword.lowercase())) {
                return TransactionDirection.SENT
            }
        }

        // Regex fallbacks
        val debitPatterns = regexRuleProvider.getDebitPatterns(source)
        for (regex in debitPatterns) {
            if (regex.containsMatchIn(text)) {
                return TransactionDirection.SENT
            }
        }
        val creditPatterns = regexRuleProvider.getCreditPatterns(source)
        for (regex in creditPatterns) {
            if (regex.containsMatchIn(text)) {
                return TransactionDirection.RECEIVED
            }
        }

        return TransactionDirection.UNKNOWN
    }

    protected open fun extractStatus(text: String): TransactionStatus {
        val lowerText = text.lowercase()

        val failedKeywords = keywordRuleProvider.getFailedKeywords(source)
        for (keyword in failedKeywords) {
            if (lowerText.contains(keyword.lowercase())) {
                return TransactionStatus.FAILED
            }
        }

        val pendingKeywords = keywordRuleProvider.getPendingKeywords(source)
        for (keyword in pendingKeywords) {
            if (lowerText.contains(keyword.lowercase())) {
                return TransactionStatus.PENDING
            }
        }

        val successKeywords = keywordRuleProvider.getSuccessKeywords(source)
        for (keyword in successKeywords) {
            if (lowerText.contains(keyword.lowercase())) {
                return TransactionStatus.SUCCESS
            }
        }

        return TransactionStatus.SUCCESS // default to success if there are no failure indicators
    }

    protected open fun cleanCounterparty(name: String): String {
        return name
            .replace(Regex("(?i)successful"), "")
            .replace(Regex("(?i)pending"), "")
            .replace(Regex("(?i)completed"), "")
            .replace(Regex("(?i)to your bank account"), "")
            .replace(Regex("[\\.\\*]"), "")
            .trim()
    }

    protected open fun calculateConfidence(
        amount: Double,
        direction: TransactionDirection,
        counterparty: String,
        refId: String?
    ): Float {
        var score = 0f
        if (amount > 0.0) score += 0.4f
        if (direction != TransactionDirection.UNKNOWN) score += 0.2f
        if (counterparty.isNotBlank() && counterparty != "Unknown UPI Merchant") score += 0.2f
        if (!refId.isNullOrBlank()) score += 0.2f
        return score
    }
}
