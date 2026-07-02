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
import com.example.domain.parser.validation.ExpectedType
import com.example.domain.parser.validation.NotificationSampleProvider
import com.example.domain.parser.validation.ParserValidationFramework
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ParserValidationFrameworkTest {

    private lateinit var normalizer: NotificationNormalizerImpl
    private lateinit var regexRuleProvider: RegexRuleProviderImpl
    private lateinit var keywordRuleProvider: KeywordRuleProviderImpl
    private lateinit var validator: TransactionValidatorImpl
    private lateinit var duplicateDetector: DuplicateDetectorImpl
    private lateinit var parserRegistry: ParserRegistryImpl
    private lateinit var parserFactory: ParserFactoryImpl
    private lateinit var parserEngine: ParserEngineImpl
    private lateinit var validationFramework: ParserValidationFramework

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

        validationFramework = ParserValidationFramework(parserEngine, duplicateDetector)
    }

    @Test
    fun testNotificationNormalizer_HindiAndMixed() {
        // Test mixed Hindi and English
        val inputHindiReceived = "Rs 150 प्राप्त हुए"
        val normalizedReceived = normalizer.normalize(inputHindiReceived)
        assertTrue(normalizedReceived.contains("received", ignoreCase = true))

        val inputHindiSent = "Ramesh Kumar को Rs 150 भेजे गए"
        val normalizedSent = normalizer.normalize(inputHindiSent)
        assertTrue(normalizedSent.contains("sent", ignoreCase = true))

        val rupeeSymbol = "₹ 2,500.50 paid to Merchant"
        val normalizedRupee = normalizer.normalize(rupeeSymbol)
        assertTrue(normalizedRupee.contains("Rs 2,500.50", ignoreCase = true))
    }

    @Test
    fun testTransactionValidator_PromoFiltering() {
        // Validation: Genuine transaction should not be promotional
        val genuineText = "Paid Rs 500 to Dad. Ref: 123456789012"
        assertFalse(validator.isPromotionalOrNonTransaction(normalizer.normalize(genuineText)))

        // Promotional offers should be recognized as such
        val discountText = "Get flat Rs 100 cashback on your next DTH recharge using Paytm!"
        assertTrue(validator.isPromotionalOrNonTransaction(normalizer.normalize(discountText)))

        val securityText = "New login detected on your PhonePe account from Samsung S24"
        assertTrue(validator.isPromotionalOrNonTransaction(normalizer.normalize(securityText)))

        val kycText = "Your Paytm wallet KYC is pending. Please upload Aadhaar to verify."
        assertTrue(validator.isPromotionalOrNonTransaction(normalizer.normalize(kycText)))
    }

    @Test
    fun testRegexRuleProvider_GPayExtraction() {
        val rawText = "You paid Rs 150 to Ramesh Kumar successful. UPI Ref: 312345678901"
        val normalized = normalizer.normalize(rawText)

        // Amount extraction
        val amountPatterns = regexRuleProvider.getAmountPatterns(NotificationSource.GOOGLE_PAY)
        var parsedAmount = 0.0
        for (regex in amountPatterns) {
            val match = regex.find(normalized)
            if (match != null) {
                parsedAmount = match.groups[1]?.value?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                break
            }
        }
        assertEquals(150.0, parsedAmount, 0.001)

        // Ref ID extraction
        val refPatterns = regexRuleProvider.getReferencePatterns(NotificationSource.GOOGLE_PAY)
        var refId: String? = null
        for (regex in refPatterns) {
            val match = regex.find(normalized)
            if (match != null) {
                refId = match.groups[1]?.value?.trim()
                break
            }
        }
        assertEquals("312345678901", refId)

        // Counterparty extraction
        val counterpartyPatterns = regexRuleProvider.getCounterpartyPatterns(NotificationSource.GOOGLE_PAY)
        var counterparty: String? = null
        for (regex in counterpartyPatterns) {
            val match = regex.find(normalized)
            if (match != null) {
                counterparty = match.groups[1]?.value?.trim()
                break
            }
        }
        assertEquals("Ramesh Kumar", counterparty)
    }

    @Test
    fun testDuplicateDetector_RawAndParsed() {
        duplicateDetector.clearCache()

        val notif = NotificationData(
            packageName = NotificationSource.PHONEPE.packageName,
            title = "PhonePe",
            text = "Paid Rs 350 to Sharma Grocery. Txn 234567123456",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 1111,
            notificationKey = "test_key_1111",
            tag = null,
            extras = emptyMap()
        )

        // Initial submission
        assertFalse(duplicateDetector.isDuplicate(notif))
        duplicateDetector.registerProcessed(notif)

        // Identical submission
        assertTrue(duplicateDetector.isDuplicate(notif))
    }

    @Test
    fun testCompleteRegressionValidationSuite() {
        // Execute the entire list of 20+ scenarios defined in NotificationSampleProvider
        val report = validationFramework.runValidationSuite()

        // Report summary assertions
        assertNotNull(report)
        assertTrue("Regression tests should have run", report.totalTests > 0)
        
        if (report.failedTests > 0) {
            val failureMessages = report.results
                .filter { !it.passed }
                .map { "${it.scenario} (${it.sampleId}): actualResult='${it.actualResult}', errorDetails='${it.errorDetails}'" }
                .joinToString("\n")
            fail("Some regression tests failed:\n$failureMessages")
        }
        
        assertTrue("Duplicate test should pass", report.duplicateTestPassed)
        assertNotNull("Performance benchmark should have run", report.benchmarkReport)
        
        // Print regression suite metrics for continuous integration log viewing
        println("=== RECOVERY & REGRESSION REPORT ===")
        println("Total Cases Executed: ${report.totalTests}")
        println("Passed: ${report.passedTests}")
        println("Failed: ${report.failedTests}")
        println("Success Rate: ${report.overallSuccessRate}%")
        println("Regression Suite Latency: ${report.totalExecutionTimeMs} ms")
        println("Duplicate Check Status: ${report.duplicateTestDetails}")
        if (report.benchmarkReport != null) {
            println("--- BENCHMARK SUITE ---")
            println("Throughput: ${report.benchmarkReport.throughputPerSec} parses/sec")
            println("Average Latency: ${report.benchmarkReport.avgLatencyMs} ms")
            println("Benchmark Memory Overhead: ${report.benchmarkReport.memoryDeltaKb} KB")
        }
    }
}
