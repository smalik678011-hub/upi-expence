package com.example.data.parser

import com.example.domain.model.NotificationSource
import com.example.domain.parser.KeywordRuleProvider

class KeywordRuleProviderImpl : KeywordRuleProvider {

    private val promoKeywords = listOf(
        "offer", "deal", "discount", "save up to", "win up to", "winner", "scratch card", "scratchcard",
        "voucher", "coupon", "promo", "sale", "free", "gift", "reward", "cashback", "cash back",
        "jackpot", "invite friends"
    )

    private val cashbackKeywords = listOf(
        "cashback", "reward", "scratch card", "refunded"
    )

    private val nonTransactionKeywords = listOf(
        // Loan & credit score
        "loan", "pre-approved", "credit score", "credit limit", "emi", "borrow",
        // Bill reminders
        "bill due", "payment due", "due date", "recharge now", "bill reminder", "recharge", "electricity bill",
        // Security alerts & sign-ins
        "logged in", "sign-in", "security alert", "password", "otp", "code", "login", "device",
        // Announcements / Updates / Ads
        "announcement", "update app", "new feature", "version", "install", "introducing", "news",
        // KYC
        "kyc", "re-kyc", "verify identity", "verification required", "documents"
    )

    private val generalDebitKeywords = listOf("paid", "sent", "debited", "transferred", "withdrew", "payment of", "transaction of", "payment to", "transaction to")
    private val generalCreditKeywords = listOf("paid you", "received", "credited", "added", "deposited")

    private val generalPendingKeywords = listOf("pending", "processing", "initiated", "in progress")
    private val generalFailedKeywords = listOf("failed", "declined", "rejected", "unsuccessful", "cancelled")
    private val generalSuccessKeywords = listOf("successful", "done", "completed", "success", "sent successfully")

    override fun getPromoKeywords(): List<String> {
        return promoKeywords + nonTransactionKeywords
    }

    override fun getCashbackKeywords(): List<String> {
        return cashbackKeywords
    }

    override fun getDebitKeywords(source: NotificationSource): List<String> {
        return generalDebitKeywords
    }

    override fun getCreditKeywords(source: NotificationSource): List<String> {
        return generalCreditKeywords
    }

    override fun getPendingKeywords(source: NotificationSource): List<String> {
        return generalPendingKeywords
    }

    override fun getFailedKeywords(source: NotificationSource): List<String> {
        return generalFailedKeywords
    }

    override fun getSuccessKeywords(source: NotificationSource): List<String> {
        return generalSuccessKeywords
    }
}
