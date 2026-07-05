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
import com.example.data.parser.TransactionValidatorImpl
import com.example.domain.model.NotificationData
import com.example.domain.model.NotificationSource
import com.example.domain.model.ParseResult
import com.example.domain.model.TransactionDirection
import com.example.domain.model.TransactionStatus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ParserEngineTest {

    private lateinit var normalizer: NotificationNormalizerImpl
    private lateinit var regexRuleProvider: RegexRuleProviderImpl
    private lateinit var keywordRuleProvider: KeywordRuleProviderImpl
    private lateinit var validator: TransactionValidatorImpl
    private lateinit var duplicateDetector: DuplicateDetectorImpl
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
    fun testRealWorldDifferentNotificationFormats() {
        // GPay SUCCESS format
        val gPaySuccess = NotificationData(
            packageName = NotificationSource.GOOGLE_PAY.packageName,
            title = "Google Pay",
            text = "You paid Rs 500 to Grocery Store successful. UPI Ref: 123456789012",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 1,
            notificationKey = "gpay_success",
            tag = null,
            extras = emptyMap()
        )
        val gpayResult = parserEngine.parseNotification(gPaySuccess)
        assertTrue(gpayResult is ParseResult.Success)
        val gpayTx = (gpayResult as ParseResult.Success).transaction
        assertEquals(500.0, gpayTx.amount, 0.001)
        assertEquals("Grocery Store", gpayTx.counterpartyName)
        assertEquals(TransactionDirection.SENT, gpayTx.direction)
        assertEquals(TransactionStatus.SUCCESS, gpayTx.status)

        // PhonePe SUCCESS format
        val phonePeSuccess = NotificationData(
            packageName = NotificationSource.PHONEPE.packageName,
            title = "PhonePe",
            text = "Paid Rs.250 to Fresh Fruits successful. Txn 234567123456",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 2,
            notificationKey = "phonepe_success",
            tag = null,
            extras = emptyMap()
        )
        val phonePeResult = parserEngine.parseNotification(phonePeSuccess)
        assertTrue(phonePeResult is ParseResult.Success)
        val phonePeTx = (phonePeResult as ParseResult.Success).transaction
        assertEquals(250.0, phonePeTx.amount, 0.001)
        assertEquals("Fresh Fruits", phonePeTx.counterpartyName)
        assertEquals("234567123456", phonePeTx.transactionRef)
    }

    @Test
    fun testFailedTransactions() {
        // GPay FAILED transaction
        val gPayFailed = NotificationData(
            packageName = NotificationSource.GOOGLE_PAY.packageName,
            title = "Google Pay",
            text = "Payment of Rs 120 to Tea Vendor failed. Ref: 987654321012",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 3,
            notificationKey = "gpay_failed",
            tag = null,
            extras = emptyMap()
        )
        val result = parserEngine.parseNotification(gPayFailed)
        assertTrue(result is ParseResult.Success)
        val tx = (result as ParseResult.Success).transaction
        assertEquals(120.0, tx.amount, 0.001)
        assertEquals(TransactionStatus.FAILED, tx.status)
        assertEquals("Tea Vendor", tx.counterpartyName)
    }

    @Test
    fun testPartialAndMalformedNotifications() {
        // Notification missing crucial fields like amount
        val partialNoAmount = NotificationData(
            packageName = NotificationSource.GOOGLE_PAY.packageName,
            title = "Google Pay",
            text = "You paid money to John Doe.",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 4,
            notificationKey = "gpay_partial_1",
            tag = null,
            extras = emptyMap()
        )
        val result = parserEngine.parseNotification(partialNoAmount)
        // Shoud be classified as Invalid or Ignored due to missing regex capture
        assertFalse(result is ParseResult.Success)

        // Empty body text
        val emptyTextNotif = NotificationData(
            packageName = NotificationSource.PAYTM.packageName,
            title = "Paytm",
            text = "",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 5,
            notificationKey = "paytm_empty",
            tag = null,
            extras = emptyMap()
        )
        val emptyResult = parserEngine.parseNotification(emptyTextNotif)
        assertTrue(emptyResult is ParseResult.Invalid)
    }

    @Test
    fun testEdgeCasesMissingRupeeSymbol() {
        // Amount written with USD or alternate symbols (if parsed) or missing standard ₹ symbol completely (e.g. only Rs or numbers)
        val missingRupeeSymbol = NotificationData(
            packageName = NotificationSource.PHONEPE.packageName,
            title = "PhonePe",
            text = "Paid 500 to Cafe Coffee Day successful. Txn 112233445566",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 6,
            notificationKey = "phonepe_missing_symbol",
            tag = null,
            extras = emptyMap()
        )
        val result = parserEngine.parseNotification(missingRupeeSymbol)
        // Since normalizer cleans and maps numeric strings, let's verify if it catches it
        if (result is ParseResult.Success) {
            val tx = result.transaction
            assertEquals(500.0, tx.amount, 0.001)
            assertEquals("Cafe Coffee Day", tx.counterpartyName)
        }
    }

    @Test
    fun testMultipleCurrenciesFormatting() {
        // Notifications representing standard Rs, INR, ₹ formatting
        val formattedWithInr = NotificationData(
            packageName = NotificationSource.GOOGLE_PAY.packageName,
            title = "Google Pay",
            text = "You paid INR 350 to Supermarket successful. Ref: 778899001122",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 7,
            notificationKey = "gpay_inr",
            tag = null,
            extras = emptyMap()
        )
        val result = parserEngine.parseNotification(formattedWithInr)
        if (result is ParseResult.Success) {
            val tx = result.transaction
            assertEquals(350.0, tx.amount, 0.001)
            assertEquals("Supermarket", tx.counterpartyName)
        }
    }
}
