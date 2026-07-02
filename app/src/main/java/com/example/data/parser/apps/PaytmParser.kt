package com.example.data.parser.apps

import com.example.data.parser.BaseNotificationParser
import com.example.domain.model.NotificationSource
import com.example.domain.parser.KeywordRuleProvider
import com.example.domain.parser.NotificationNormalizer
import com.example.domain.parser.RegexRuleProvider
import com.example.domain.parser.TransactionValidator

class PaytmParser(
    normalizer: NotificationNormalizer,
    regexRuleProvider: RegexRuleProvider,
    keywordRuleProvider: KeywordRuleProvider,
    validator: TransactionValidator
) : BaseNotificationParser(
    source = NotificationSource.PAYTM,
    normalizer = normalizer,
    regexRuleProvider = regexRuleProvider,
    keywordRuleProvider = keywordRuleProvider,
    validator = validator
)
