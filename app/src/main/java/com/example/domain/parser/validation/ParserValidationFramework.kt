package com.example.domain.parser.validation

import com.example.domain.model.NotificationData
import com.example.domain.model.ParseResult
import com.example.domain.parser.DuplicateDetector
import com.example.domain.parser.ParserEngine
import java.util.UUID

data class SingleValidationResult(
    val sampleId: String,
    val scenario: String,
    val appSource: String,
    val passed: Boolean,
    val expectedType: ExpectedType,
    val actualResult: String,
    val latencyMs: Double,
    val errorDetails: String? = null
)

data class BenchmarkResult(
    val totalProcessed: Int,
    val totalTimeMs: Long,
    val throughputPerSec: Double,
    val avgLatencyMs: Double,
    val memoryDeltaKb: Long
)

data class ValidationReport(
    val totalTests: Int,
    val passedTests: Int,
    val failedTests: Int,
    val ignoredTests: Int, // Expected to be IGNORED and was ignored
    val invalidTests: Int, // Expected to be INVALID and was invalid
    val overallSuccessRate: Double,
    val totalExecutionTimeMs: Long,
    val results: List<SingleValidationResult>,
    val benchmarkReport: BenchmarkResult?,
    val duplicateTestPassed: Boolean,
    val duplicateTestDetails: String
)

class ParserValidationFramework(
    private val parserEngine: ParserEngine,
    private val duplicateDetector: DuplicateDetector
) {

    /**
     * Runs the complete regression suite over all pre-defined notification samples.
     */
    fun runValidationSuite(): ValidationReport {
        val startTime = System.currentTimeMillis()
        val samples = NotificationSampleProvider.getSamples()
        val results = mutableListOf<SingleValidationResult>()

        var passedCount = 0
        var failedCount = 0
        var ignoredCount = 0
        var invalidCount = 0

        // Clear duplicate detector cache before running regression to ensure isolated results
        duplicateDetector.clearCache()

        for (sample in samples) {
            val sampleStartTime = System.nanoTime()
            val notifData = NotificationData(
                packageName = sample.packageName,
                title = sample.title,
                text = sample.text,
                bigText = null,
                postTime = System.currentTimeMillis(),
                notificationId = sample.id.hashCode(),
                notificationKey = sample.id + "_" + UUID.randomUUID().toString().take(4),
                tag = null,
                extras = emptyMap()
            )

            val parseResult = parserEngine.parseNotification(notifData)
            val sampleEndTime = System.nanoTime()
            val latencyMs = (sampleEndTime - sampleStartTime) / 1_000_000.0

            var passed = false
            var actualResultDesc = ""
            var errorDetails: String? = null

            when (sample.expectedType) {
                ExpectedType.SUCCESS -> {
                    if (parseResult is ParseResult.Success) {
                        val txn = parseResult.transaction
                        val expected = sample.expectedData
                        if (expected != null) {
                            val amountMatches = txn.amount == expected.amount
                            val directionMatches = txn.direction == expected.direction
                            val statusMatches = txn.status == expected.status
                            val counterpartyMatches = txn.counterpartyName.equals(expected.counterpartyName, ignoreCase = true) ||
                                    txn.counterpartyName.contains(expected.counterpartyName, ignoreCase = true) ||
                                    expected.counterpartyName == "Unknown UPI Merchant"

                            val refMatches = if (expected.transactionRef != null) {
                                txn.transactionRef == expected.transactionRef
                            } else true

                            if (amountMatches && directionMatches && statusMatches && counterpartyMatches && refMatches) {
                                passed = true
                                actualResultDesc = "SUCCESS (Rs.${txn.amount}, ${txn.direction}, ${txn.status}, ${txn.counterpartyName}, Ref: ${txn.transactionRef})"
                            } else {
                                passed = false
                                val mismatches = mutableListOf<String>()
                                if (!amountMatches) mismatches.add("Amount (Exp: ${expected.amount}, Act: ${txn.amount})")
                                if (!directionMatches) mismatches.add("Direction (Exp: ${expected.direction}, Act: ${txn.direction})")
                                if (!statusMatches) mismatches.add("Status (Exp: ${expected.status}, Act: ${txn.status})")
                                if (!counterpartyMatches) mismatches.add("Counterparty (Exp: ${expected.counterpartyName}, Act: ${txn.counterpartyName})")
                                if (!refMatches) mismatches.add("Ref (Exp: ${expected.transactionRef}, Act: ${txn.transactionRef})")
                                errorDetails = "Field mismatch: " + mismatches.joinToString(", ")
                                actualResultDesc = "SUCCESS with Field Mismatches"
                            }
                        } else {
                            passed = true
                            actualResultDesc = "SUCCESS (Extracted Rs.${txn.amount})"
                        }
                    } else {
                        passed = false
                        val innerReason = when (parseResult) {
                            is ParseResult.Ignored -> ": " + parseResult.reason
                            is ParseResult.Invalid -> ": " + parseResult.reason
                            is ParseResult.Failed -> ": " + parseResult.reason
                            else -> ""
                        }
                        errorDetails = "Expected Success but got: ${parseResult::class.simpleName}$innerReason"
                        actualResultDesc = parseResult.javaClass.simpleName
                    }
                }
                ExpectedType.IGNORED -> {
                    // Promotional / Non-transaction notifications should be Ignored
                    if (parseResult is ParseResult.Ignored) {
                        passed = true
                        actualResultDesc = "IGNORED (Reason: ${parseResult.reason})"
                    } else {
                        passed = false
                        errorDetails = "Expected Ignored but got: ${parseResult::class.simpleName}"
                        actualResultDesc = parseResult.javaClass.simpleName
                    }
                }
                ExpectedType.INVALID -> {
                    // Malformed or empty notifications should be Invalid or Ignored depending on text availability
                    if (parseResult is ParseResult.Invalid || parseResult is ParseResult.Ignored) {
                        passed = true
                        actualResultDesc = "INVALID/IGNORED AS EXPECTED"
                    } else {
                        passed = false
                        errorDetails = "Expected Invalid/Ignored but got: ${parseResult::class.simpleName}"
                        actualResultDesc = parseResult.javaClass.simpleName
                    }
                }
                ExpectedType.FAILED -> {
                    if (parseResult is ParseResult.Failed) {
                        passed = true
                        actualResultDesc = "FAILED AS EXPECTED (Reason: ${parseResult.reason})"
                    } else {
                        passed = false
                        errorDetails = "Expected Failed but got: ${parseResult::class.simpleName}"
                        actualResultDesc = parseResult.javaClass.simpleName
                    }
                }
            }

            if (passed) {
                passedCount++
                when (sample.expectedType) {
                    ExpectedType.IGNORED -> ignoredCount++
                    ExpectedType.INVALID -> invalidCount++
                    else -> {}
                }
            } else {
                failedCount++
            }

            results.add(
                SingleValidationResult(
                    sampleId = sample.id,
                    scenario = sample.scenario,
                    appSource = sample.packageName.substringAfterLast("."),
                    passed = passed,
                    expectedType = sample.expectedType,
                    actualResult = actualResultDesc,
                    latencyMs = latencyMs,
                    errorDetails = errorDetails
                )
            )
        }

        // Run isolated Duplicate Tests
        val (dupPassed, dupDetails) = runDuplicateTests()

        // Run isolated Benchmarking Test
        val benchmarkReport = runPerformanceBenchmark()

        val totalTimeMs = System.currentTimeMillis() - startTime

        return ValidationReport(
            totalTests = samples.size,
            passedTests = passedCount,
            failedTests = failedCount,
            ignoredTests = ignoredCount,
            invalidTests = invalidCount,
            overallSuccessRate = if (samples.isNotEmpty()) (passedCount.toDouble() / samples.size) * 100.0 else 0.0,
            totalExecutionTimeMs = totalTimeMs,
            results = results,
            benchmarkReport = benchmarkReport,
            duplicateTestPassed = dupPassed,
            duplicateTestDetails = dupDetails
        )
    }

    /**
     * Isolated duplicate checks validation:
     * - Verify identical notifications are flagged as duplicates.
     * - Verify parsed transaction signature collisions are caught within the sliding window.
     */
    private fun runDuplicateTests(): Pair<Boolean, String> {
        duplicateDetector.clearCache()
        
        val testNotif = NotificationData(
            packageName = "com.phonepe.app",
            title = "PhonePe",
            text = "Paid Rs 350 to Sharma Grocery. Txn 234567123456",
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = 12345,
            notificationKey = "unique_key_phonepe_12345",
            tag = null,
            extras = emptyMap()
        )

        // 1. First capture: should be parsed successfully
        val result1 = parserEngine.parseNotification(testNotif)
        if (result1 !is ParseResult.Success) {
            return Pair(false, "Duplicate Test setup failed: Original notification could not be parsed.")
        }

        // 2. Immediate second capture: should be ignored as duplicate raw notification
        val result2 = parserEngine.parseNotification(testNotif)
        if (result2 !is ParseResult.Ignored || !result2.reason.contains("Duplicate", ignoreCase = true)) {
            return Pair(false, "Failed to identify duplicate raw notification. Result was: $result2")
        }

        // 3. Different notification payload that parses to identical transaction fields:
        // E.g., different notification ID/Key, but same app, amount, and counterparty.
        val testNotif2 = NotificationData(
            packageName = "com.phonepe.app",
            title = "PhonePe",
            text = "Paid Rs 350 to Sharma Grocery. Txn 999999999999", // Different txn ID in SMS, but matches signature
            bigText = null,
            postTime = System.currentTimeMillis() + 5000,
            notificationId = 67890,
            notificationKey = "unique_key_phonepe_67890",
            tag = null,
            extras = emptyMap()
        )
        // Clear raw cache but retain parsed signatures
        // Wait, clearCache clears everything, so we manually check duplicate parsed transaction logic.
        val parseResultAnother = parserEngine.parseNotification(testNotif2)
        if (parseResultAnother !is ParseResult.Ignored || !parseResultAnother.reason.contains("Duplicate parsed", ignoreCase = true)) {
            return Pair(false, "Failed to identify duplicate parsed transaction signature within sliding window. Got: $parseResultAnother")
        }

        return Pair(true, "All duplicate detection flows validated perfectly (Raw collision & parsed signature collision).")
    }

    /**
     * Measures parsing speeds, repeated parsing overhead, memory, and high-volume simulation.
     */
    fun runPerformanceBenchmark(): BenchmarkResult {
        val samples = NotificationSampleProvider.getSamples().filter { it.expectedType == ExpectedType.SUCCESS }
        if (samples.isEmpty()) {
            return BenchmarkResult(0, 0L, 0.0, 0.0, 0L)
        }

        val runtime = Runtime.getRuntime()
        System.gc() // Suggest garbage collection to baseline memory measurements
        Thread.sleep(50L)
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        val iterationCount = 100 // Repetitive Stress Testing
        val totalToProcess = samples.size * iterationCount

        val startTime = System.currentTimeMillis()
        
        // We use fresh notification keys to bypass duplicate checking cache
        for (i in 1..iterationCount) {
            for (sample in samples) {
                val notif = NotificationData(
                    packageName = sample.packageName,
                    title = sample.title,
                    text = sample.text,
                    bigText = null,
                    postTime = System.currentTimeMillis(),
                    notificationId = (sample.id + "_bench_$i").hashCode(),
                    notificationKey = "key_bench_${sample.id}_$i",
                    tag = null,
                    extras = emptyMap()
                )
                parserEngine.parseNotification(notif)
            }
        }

        val endTime = System.currentTimeMillis()
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()

        val totalTimeMs = endTime - startTime
        val throughputPerSec = if (totalTimeMs > 0) (totalToProcess.toDouble() / totalTimeMs) * 1000.0 else 0.0
        val avgLatencyMs = if (totalToProcess > 0) totalTimeMs.toDouble() / totalToProcess else 0.0
        val memoryDeltaKb = (finalMemory - initialMemory) / 1024

        return BenchmarkResult(
            totalProcessed = totalToProcess,
            totalTimeMs = totalTimeMs,
            throughputPerSec = throughputPerSec,
            avgLatencyMs = avgLatencyMs,
            memoryDeltaKb = if (memoryDeltaKb < 0) 0L else memoryDeltaKb
        )
    }
}
