package com.example.domain.parser

import com.example.domain.model.NotificationSource

interface KeywordRuleProvider {
    fun getPromoKeywords(): List<String>
    fun getCashbackKeywords(): List<String>
    fun getDebitKeywords(source: NotificationSource): List<String>
    fun getCreditKeywords(source: NotificationSource): List<String>
    fun getPendingKeywords(source: NotificationSource): List<String>
    fun getFailedKeywords(source: NotificationSource): List<String>
    fun getSuccessKeywords(source: NotificationSource): List<String>
}
