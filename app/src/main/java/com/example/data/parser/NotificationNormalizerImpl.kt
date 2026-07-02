package com.example.data.parser

import com.example.domain.parser.NotificationNormalizer

class NotificationNormalizerImpl : NotificationNormalizer {
    override fun normalize(text: String): String {
        if (text.isBlank()) return ""

        // 1. Remove control characters and invisible unicode characters
        var normalized = text.replace(Regex("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]"), " ")

        // 2. Normalize common mixed Hindi/English transaction keywords to English counterparts
        normalized = normalized
            .replace("प्राप्त हुए", "received")
            .replace("प्राप्त हुआ", "received")
            .replace("भुगतान किया", "paid")
            .replace("भेजे गए", "sent")
            .replace("भेजा गया", "sent")
            .replace("सफल रहा", "successful")
            .replace("खाते में", "account")
            .replace("जमा किए गए", "credited")

        // 3. Normalize multiple styles of Indian Currency indicators to standard "Rs." with space
        // Replacing ₹, INR, Rs., Rs to "Rs"
        normalized = normalized
            .replace("₹", "Rs")
            .replace("INR", "Rs")
            .replace(Regex("Rs\\.?"), "Rs")

        // 4. Normalize line breaks, carriage returns, and tabs to standard spaces
        normalized = normalized.replace(Regex("[\\n\\r\\t]"), " ")

        // 5. Compress multiple contiguous whitespace spaces into a single space
        normalized = normalized.replace(Regex("\\s+"), " ").trim()

        return normalized
    }
}
