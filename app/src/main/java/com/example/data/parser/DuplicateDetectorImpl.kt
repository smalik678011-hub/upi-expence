package com.example.data.parser

import com.example.domain.model.NotificationData
import com.example.domain.model.ParsedTransaction
import com.example.domain.parser.DuplicateDetector
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class DuplicateDetectorImpl : DuplicateDetector {

    // Store recent raw notification keys and content hashes with their processing timestamp
    private val rawNotificationKeys = ConcurrentHashMap<String, Long>()
    private val rawNotificationHashes = ConcurrentHashMap<Int, Long>()

    // Store recent transaction signatures with their parsed timestamps
    private val parsedSignatures = ConcurrentHashMap<String, Long>()

    // Sliding window durations: 5 minutes for raw notifications, 2 minutes for identical parsed transactions
    private val rawWindowMillis = TimeUnit.MINUTES.toMillis(5)
    private val parsedWindowMillis = TimeUnit.MINUTES.toMillis(2)

    override fun isDuplicate(notification: NotificationData): Boolean {
        val now = System.currentTimeMillis()
        pruneOldEntries(now)

        // 1. Check unique notification key from system if available
        notification.notificationKey?.let { key ->
            if (rawNotificationKeys.containsKey(key)) {
                return true
            }
        }

        // 2. Check content hash of packageName, title, and body
        val textToHash = "${notification.packageName}|${notification.title}|${notification.text}"
        val hash = textToHash.hashCode()
        if (rawNotificationHashes.containsKey(hash)) {
            val prevTime = rawNotificationHashes[hash] ?: 0L
            if (now - prevTime < rawWindowMillis) {
                return true
            }
        }

        return false
    }

    private fun getSignature(transaction: ParsedTransaction): String {
        val ref = transaction.transactionRef?.trim()
        return if (!ref.isNullOrEmpty()) {
            "${transaction.sourceApp.packageName}|${transaction.amount}|${transaction.counterpartyName.lowercase().trim()}|$ref"
        } else {
            "${transaction.sourceApp.packageName}|${transaction.amount}|${transaction.counterpartyName.lowercase().trim()}|no_ref"
        }
    }

    override fun isDuplicateParsed(transaction: ParsedTransaction): Boolean {
        val now = System.currentTimeMillis()
        pruneOldEntries(now)

        val signature = getSignature(transaction)
        
        if (parsedSignatures.containsKey(signature)) {
            val prevTime = parsedSignatures[signature] ?: 0L
            // If an identical transaction from the same source is parsed within 2 minutes, treat as duplicate
            if (now - prevTime < parsedWindowMillis) {
                return true
            }
        }

        return false
    }

    override fun registerProcessed(notification: NotificationData) {
        val now = System.currentTimeMillis()
        notification.notificationKey?.let { key ->
            rawNotificationKeys[key] = now
        }
        val textToHash = "${notification.packageName}|${notification.title}|${notification.text}"
        rawNotificationHashes[textToHash.hashCode()] = now
    }

    override fun registerProcessedParsed(transaction: ParsedTransaction) {
        val now = System.currentTimeMillis()
        val signature = getSignature(transaction)
        parsedSignatures[signature] = now
    }

    override fun clearCache() {
        rawNotificationKeys.clear()
        rawNotificationHashes.clear()
        parsedSignatures.clear()
    }

    private fun pruneOldEntries(currentTime: Long) {
        // Prune raw notifications exceeding rawWindowMillis
        val rawKeysIterator = rawNotificationKeys.entries.iterator()
        while (rawKeysIterator.hasNext()) {
            val entry = rawKeysIterator.next()
            if (currentTime - entry.value > rawWindowMillis) {
                rawKeysIterator.remove()
            }
        }

        val rawHashesIterator = rawNotificationHashes.entries.iterator()
        while (rawHashesIterator.hasNext()) {
            val entry = rawHashesIterator.next()
            if (currentTime - entry.value > rawWindowMillis) {
                rawHashesIterator.remove()
            }
        }

        // Prune parsed signatures exceeding parsedWindowMillis
        val parsedIterator = parsedSignatures.entries.iterator()
        while (parsedIterator.hasNext()) {
            val entry = parsedIterator.next()
            if (currentTime - entry.value > parsedWindowMillis) {
                parsedIterator.remove()
            }
        }
    }
}
