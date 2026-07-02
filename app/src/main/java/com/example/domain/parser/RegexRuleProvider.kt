package com.example.domain.parser

import com.example.domain.model.NotificationSource

interface RegexRuleProvider {
    fun getAmountPatterns(source: NotificationSource): List<Regex>
    fun getReferencePatterns(source: NotificationSource): List<Regex>
    fun getCounterpartyPatterns(source: NotificationSource): List<Regex>
    fun getDebitPatterns(source: NotificationSource): List<Regex>
    fun getCreditPatterns(source: NotificationSource): List<Regex>
}
