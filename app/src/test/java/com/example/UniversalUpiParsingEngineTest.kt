package com.example

import com.example.core.log.Logger
import com.example.data.parser.DuplicateDetectorImpl
import com.example.data.parser.KeywordRuleProviderImpl
import com.example.data.parser.NotificationNormalizerImpl
import com.example.data.parser.ParserEngineImpl
import com.example.data.parser.ParserFactoryImpl
import com.example.data.parser.ParserLoggerImpl
import com.example.data.parser.ParserRegistryImpl
import com.example.data.parser.RegexRuleProviderImpl
import com.example.data.parser.TransactionMapperImpl
import com.example.data.parser.TransactionValidatorImpl
import com.example.domain.model.NotificationData
import com.example.domain.model.NotificationSource
import com.example.domain.model.ParseResult
import com.example.domain.model.TransactionDirection
import com.example.domain.model.TransactionStatus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UniversalUpiParsingEngineTest {

    private lateinit var normalizer: NotificationNormalizerImpl
    private lateinit var regexRuleProvider: RegexRuleProviderImpl
    private lateinit var keywordRuleProvider: KeywordRuleProviderImpl
    private lateinit var validator: TransactionValidatorImpl
    private lateinit var duplicateDetector: DuplicateDetectorImpl
    private lateinit var mapper: TransactionMapperImpl
    private lateinit var parserRegistry: ParserRegistryImpl
    private lateinit var parserFactory: ParserFactoryImpl
    private lateinit var parserEngine: ParserEngineImpl

    private val mockLogger = object : Logger {
        override fun d(tag: String, message: String) {}
        override fun i(tag: String, message: String) {}
        override fun w(tag: String, message: String, throwable: Throwable?) {}
        override fun e(tag: String, message: String, throwable: Throwable?) {}
    }

    private val parserLogger = ParserLoggerImpl(mockLogger)

    @Before
    fun setUp() {
        normalizer = NotificationNormalizerImpl()
        regexRuleProvider = RegexRuleProviderImpl()
        keywordRuleProvider = KeywordRuleProviderImpl()
        validator = TransactionValidatorImpl(keywordRuleProvider)
        duplicateDetector = DuplicateDetectorImpl()
        mapper = TransactionMapperImpl()
        parserRegistry = ParserRegistryImpl()
        parserFactory = ParserFactoryImpl(normalizer, regexRuleProvider, keywordRuleProvider, validator)
        
        parserEngine = ParserEngineImpl(
            registry = parserRegistry,
            factory = parserFactory,
            duplicateDetector = duplicateDetector,
            logger = parserLogger
        )
    }

    @Test
    fun testNotificationNormalizer() {
        // Test unicode cleanup & whitespace compaction
        val input = "  \n  Paytm: ₹ 1,500.00\t paid to   Amit Sharma.  "
        val normalized = normalizer.normalize(input)
        assertEquals("Paytm: Rs 1,500.00 paid to Amit Sharma.", normalized)

        // Test mixed Hindi to English translation normalization
        val hindiText = "Rs. 250 प्राप्त हुए"
        val normalizedHindi = normalizer.normalize(hindiText)
        assertTrue(normalizedHindi.contains("received", ignoreCase = true))

        val hindiPaid = "Rs. 300 का भुगतान किया"
        val normalizedPaid = normalizer.normalize(hindiPaid)
        assertTrue(normalizedPaid.contains("paid", ignoreCase = true))
    }

    @Test
    fun testTransactionValidator() {
        // Validation: Genuine transactions should NOT be flagged as promo
        val genuineNotificationText = "Paid Rs 150 to Sharma Grocery. Ref: 123456789012"
        assertFalse(validator.isPromotionalOrNonTransaction(normalizer.normalize(genuineNotificationText)))

        // Validation: Offers, ads, app updates, etc. SHOULD be flagged as promo/non-transaction
        val kycNotificationText = "Complete your KYC before next week or your account will be suspended!"
        assertTrue(validator.isPromotionalOrNonTransaction(normalizer.normalize(kycNotificationText)))

        val discountText = "Flat 50% discount on your next ride, win scratch card up to Rs 200!"
        assertTrue(validator.isPromotionalOrNonTransaction(normalizer.normalize(discountText)))

        val loanText = "Get pre-approved EMI loans up to 5 Lakhs instantly with Navi!"
        assertTrue(validator.isPromotionalOrNonTransaction(normalizer.normalize(loanText)))
    }

    @Test
    fun testDuplicateDetector() {
        duplicateDetector.clearCache()

        val notification = NotificationData(
            packageName = NotificationSource.GOOGLE_PAY.packageName,
            title = "Google Pay",
            text = "You paid Rs 150 to Amit Sharma Ref: 456712348901",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 1,
            notificationKey = "key_1",
            tag = null,
            extras = emptyMap()
        )

        // 1st raw check should be unique
        assertFalse(duplicateDetector.isDuplicate(notification))
        duplicateDetector.registerProcessed(notification)

        // 2nd raw check should be duplicate
        assertTrue(duplicateDetector.isDuplicate(notification))

        // Duplicate checks based on content hash
        val notificationWithSameText = NotificationData(
            packageName = NotificationSource.GOOGLE_PAY.packageName,
            title = "Google Pay",
            text = "You paid Rs 150 to Amit Sharma Ref: 456712348901",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 2,
            notificationKey = "key_2",
            tag = null,
            extras = emptyMap()
        )
        assertTrue(duplicateDetector.isDuplicate(notificationWithSameText))
    }

    @Test
    fun testGooglePayParser() {
        val rawText = "You paid Rs 250 to Ramesh Kumar successful. UPI Ref: 312345678901"
        val notification = NotificationData(
            packageName = NotificationSource.GOOGLE_PAY.packageName,
            title = "Google Pay",
            text = rawText,
            bigText = null,
            postTime = 1672531199000L,
            notificationId = 10,
            notificationKey = "gpay_txn_1",
            tag = null,
            extras = emptyMap()
        )

        val parser = parserRegistry.getParserForPackage(NotificationSource.GOOGLE_PAY.packageName)
        assertNotNull(parser)

        val result = parser!!.parse(notification)
        assertTrue(result is ParseResult.Success)

        val transaction = (result as ParseResult.Success).transaction
        assertEquals(250.0, transaction.amount, 0.001)
        assertEquals(TransactionDirection.SENT, transaction.direction)
        assertEquals("Ramesh Kumar", transaction.counterpartyName)
        assertEquals("312345678901", transaction.transactionRef)
        assertEquals(TransactionStatus.SUCCESS, transaction.status)
        assertEquals(NotificationSource.GOOGLE_PAY, transaction.sourceApp)
    }

    @Test
    fun testPhonePeParser() {
        val rawText = "Paid Rs. 500 to John Doe successful. UTR: 412345678902"
        val notification = NotificationData(
            packageName = NotificationSource.PHONEPE.packageName,
            title = "PhonePe",
            text = rawText,
            bigText = null,
            postTime = 1672531199000L,
            notificationId = 11,
            notificationKey = "phonepe_txn_1",
            tag = null,
            extras = emptyMap()
        )

        val parser = parserRegistry.getParserForPackage(NotificationSource.PHONEPE.packageName)
        assertNotNull(parser)

        val result = parser!!.parse(notification)
        assertTrue(result is ParseResult.Success)

        val transaction = (result as ParseResult.Success).transaction
        assertEquals(500.0, transaction.amount, 0.001)
        assertEquals(TransactionDirection.SENT, transaction.direction)
        assertEquals("John Doe", transaction.counterpartyName)
        assertEquals("412345678902", transaction.transactionRef)
    }

    @Test
    fun testPaytmParser() {
        val rawText = "Received Rs.1,200 from Sonal Gupta successful. Ref No: 512345678903"
        val notification = NotificationData(
            packageName = NotificationSource.PAYTM.packageName,
            title = "Paytm",
            text = rawText,
            bigText = null,
            postTime = 1672531199000L,
            notificationId = 12,
            notificationKey = "paytm_txn_1",
            tag = null,
            extras = emptyMap()
        )

        val parser = parserRegistry.getParserForPackage(NotificationSource.PAYTM.packageName)
        assertNotNull(parser)

        val result = parser!!.parse(notification)
        assertTrue(result is ParseResult.Success)

        val transaction = (result as ParseResult.Success).transaction
        assertEquals(1200.0, transaction.amount, 0.001)
        assertEquals(TransactionDirection.RECEIVED, transaction.direction)
        assertEquals("Sonal Gupta", transaction.counterpartyName)
        assertEquals("512345678903", transaction.transactionRef)
    }

    @Test
    fun testNaviParser() {
        val rawText = "Paid Rs.75 to Navi Merchant Ref: 612345678904"
        val notification = NotificationData(
            packageName = NotificationSource.NAVI.packageName,
            title = "Navi",
            text = rawText,
            bigText = null,
            postTime = 1672531199000L,
            notificationId = 13,
            notificationKey = "navi_txn_1",
            tag = null,
            extras = emptyMap()
        )

        val parser = parserRegistry.getParserForPackage(NotificationSource.NAVI.packageName)
        assertNotNull(parser)

        val result = parser!!.parse(notification)
        assertTrue(result is ParseResult.Success)

        val transaction = (result as ParseResult.Success).transaction
        assertEquals(75.0, transaction.amount, 0.001)
        assertEquals(TransactionDirection.SENT, transaction.direction)
        assertEquals("Navi Merchant", transaction.counterpartyName)
        assertEquals("612345678904", transaction.transactionRef)
    }

    @Test
    fun testParserEngineEndToEnd() {
        duplicateDetector.clearCache()

        val validNotification = NotificationData(
            packageName = NotificationSource.GOOGLE_PAY.packageName,
            title = "Google Pay",
            text = "You paid Rs 350 to Big Basket successful. Ref: 111122223333",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 100,
            notificationKey = "end_to_end_1",
            tag = null,
            extras = emptyMap()
        )

        // 1. First parse should be Success
        val result1 = parserEngine.parseNotification(validNotification)
        assertTrue(result1 is ParseResult.Success)
        val transaction = (result1 as ParseResult.Success).transaction
        assertEquals(350.0, transaction.amount, 0.001)
        assertEquals("Big Basket", transaction.counterpartyName)

        // 2. Immediate identical notification should be Ignored as Duplicate Raw Notification
        val result2 = parserEngine.parseNotification(validNotification)
        assertTrue(result2 is ParseResult.Ignored)
        assertEquals("Duplicate raw notification", (result2 as ParseResult.Ignored).reason)
    }

    @Test
    fun testTransactionMapper() {
        val rawText = "Paid Rs 100 to Chai Tapri. Ref: 987654321098"
        val notification = NotificationData(
            packageName = NotificationSource.GOOGLE_PAY.packageName,
            title = "Google Pay",
            text = rawText,
            bigText = null,
            postTime = 1672531199000L,
            notificationId = 200,
            notificationKey = "mapper_test_1",
            tag = null,
            extras = emptyMap()
        )

        val parser = parserRegistry.getParserForPackage(NotificationSource.GOOGLE_PAY.packageName)!!
        val result = parser.parse(notification) as ParseResult.Success
        
        val expense = mapper.toExpense(result.transaction)
        
        assertEquals(100.0, expense.amount, 0.001)
        assertEquals("Chai Tapri", expense.merchantName)
        assertEquals("DEBIT", expense.uType)
        assertEquals("987654321098", expense.transactionRef)
        assertEquals("Google Pay", expense.accountOrBank)
    }

    @Test
    fun testDuplicateDetectorParsedWithAndWithoutReference() {
        duplicateDetector.clearCache()

        val baseTx = com.example.domain.model.ParsedTransaction(
            amount = 150.0,
            currency = "INR",
            direction = TransactionDirection.SENT,
            status = TransactionStatus.SUCCESS,
            counterpartyName = "Amit Sharma",
            transactionRef = "REF123456",
            timestamp = System.currentTimeMillis(),
            sourceApp = NotificationSource.GOOGLE_PAY,
            rawNotification = "raw",
            confidence = 1.0f
        )

        // 1. Initially, not duplicate
        assertFalse(duplicateDetector.isDuplicateParsed(baseTx))
        duplicateDetector.registerProcessedParsed(baseTx)

        // 2. Same transaction (same reference) within 2 minutes: duplicate
        assertTrue(duplicateDetector.isDuplicateParsed(baseTx))

        // 3. Different reference, same amount, same counterparty: should NOT be duplicate
        val distinctTx = baseTx.copy(transactionRef = "REF999999")
        assertFalse(duplicateDetector.isDuplicateParsed(distinctTx))

        // 4. Clean cache
        duplicateDetector.clearCache()

        // 5. Without reference (null / empty) should fall back to standard signature
        val txNoRef1 = baseTx.copy(transactionRef = null)
        val txNoRef2 = baseTx.copy(transactionRef = "  ")

        assertFalse(duplicateDetector.isDuplicateParsed(txNoRef1))
        duplicateDetector.registerProcessedParsed(txNoRef1)

        // Same amount, same counterparty, both without reference within 2 minutes: duplicate
        assertTrue(duplicateDetector.isDuplicateParsed(txNoRef2))
    }
}
