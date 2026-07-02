package com.example.data.parser

import com.example.domain.model.ParsedTransaction
import com.example.domain.parser.KeywordRuleProvider
import com.example.domain.parser.TransactionValidator

class TransactionValidatorImpl(
    private val keywordRuleProvider: KeywordRuleProvider
) : TransactionValidator {

    override fun isPromotionalOrNonTransaction(text: String): Boolean {
        if (text.isBlank()) return true
        val lowerText = text.lowercase()
        
        // Smart bypass for genuine financial transactions containing cashbacks/refunds
        val hasAmount = lowerText.contains("rs") || lowerText.contains("₹")
        val hasTransactionVerb = lowerText.contains("received") || 
                                 lowerText.contains("credited") || 
                                 lowerText.contains("refunded") || 
                                 lowerText.contains("paid") || 
                                 lowerText.contains("sent") || 
                                 lowerText.contains("transferred") ||
                                 lowerText.contains("debited")
        
        val isExplicitPromoOnly = lowerText.contains("win up to") || 
                                  lowerText.contains("save up to") || 
                                  lowerText.contains("scratch card waiting") || 
                                  lowerText.contains("apply now") ||
                                  lowerText.contains("pre-approved")
        
        if (hasAmount && hasTransactionVerb && !isExplicitPromoOnly) {
            return false // Treat as genuine transaction
        }

        // Fetch all promotional, cashback, loan, kyc, and other non-transaction keywords
        val promoKeywords = keywordRuleProvider.getPromoKeywords()
        
        for (keyword in promoKeywords) {
            if (lowerText.contains(keyword.lowercase())) {
                return true
            }
        }
        return false
    }

    override fun validateParsed(transaction: ParsedTransaction): Boolean {
        // A valid parsed transaction must have a positive amount and a non-empty counterparty name
        if (transaction.amount <= 0.0) return false
        if (transaction.counterpartyName.isBlank()) return false
        return true
    }
}
