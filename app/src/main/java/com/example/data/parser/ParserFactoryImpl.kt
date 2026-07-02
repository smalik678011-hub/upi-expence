package com.example.data.parser

import com.example.data.parser.apps.GooglePayParser
import com.example.data.parser.apps.PhonePeParser
import com.example.data.parser.apps.PaytmParser
import com.example.data.parser.apps.NaviParser
import com.example.domain.model.NotificationSource
import com.example.domain.parser.KeywordRuleProvider
import com.example.domain.parser.NotificationNormalizer
import com.example.domain.parser.NotificationParser
import com.example.domain.parser.ParserFactory
import com.example.domain.parser.RegexRuleProvider
import com.example.domain.parser.TransactionValidator

class ParserFactoryImpl(
    private val normalizer: NotificationNormalizer,
    private val regexRuleProvider: RegexRuleProvider,
    private val keywordRuleProvider: KeywordRuleProvider,
    private val validator: TransactionValidator
) : ParserFactory {

    override fun createParser(source: NotificationSource): NotificationParser? {
        return when (source) {
            NotificationSource.GOOGLE_PAY -> GooglePayParser(normalizer, regexRuleProvider, keywordRuleProvider, validator)
            NotificationSource.PHONEPE -> PhonePeParser(normalizer, regexRuleProvider, keywordRuleProvider, validator)
            NotificationSource.PAYTM -> PaytmParser(normalizer, regexRuleProvider, keywordRuleProvider, validator)
            NotificationSource.NAVI -> NaviParser(normalizer, regexRuleProvider, keywordRuleProvider, validator)
            else -> null
        }
    }
}
