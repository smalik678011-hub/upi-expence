package com.example.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object IndianFormattingUtils {
    
    /**
     * Formats an amount into Indian Rupee format with Indian grouping (Lakh/Crore), e.g., ₹ 12,34,567.89
     */
    fun formatIndianCurrency(amount: Double): String {
        val numberString = String.format(Locale.US, "%.2f", amount)
        val parts = numberString.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) "." + parts[1] else ".00"
        
        val len = integerPart.length
        if (len <= 3) {
            return "₹ " + integerPart + decimalPart
        }
        
        val lastThree = integerPart.substring(len - 3)
        val remaining = integerPart.substring(0, len - 3)
        
        val builder = StringBuilder()
        var i = remaining.length - 1
        var count = 0
        while (i >= 0) {
            builder.append(remaining[i])
            count++
            if (count == 2 && i > 0) {
                builder.append(",")
                count = 0
            }
            i--
        }
        
        val formattedRemaining = builder.reverse().toString()
        return "₹ " + formattedRemaining + "," + lastThree + decimalPart
    }

    /**
     * Formats dates standard to Indian requirements, e.g., "02 Jul 2026, 08:33 AM"
     */
    fun formatIndianDate(date: Date): String {
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("en", "IN"))
        return formatter.format(date)
    }

    /**
     * Formats standard Indian numbers with Lakh/Crore grouping, e.g. 1,00,000 instead of 100,000
     */
    fun formatIndianNumber(number: Long): String {
        val str = number.toString()
        val len = str.length
        if (len <= 3) return str
        
        val lastThree = str.substring(len - 3)
        val remaining = str.substring(0, len - 3)
        
        val builder = StringBuilder()
        var i = remaining.length - 1
        var count = 0
        while (i >= 0) {
            builder.append(remaining[i])
            count++
            if (count == 2 && i > 0) {
                builder.append(",")
                count = 0
            }
            i--
        }
        val formattedRemaining = builder.reverse().toString()
        return formattedRemaining + "," + lastThree
    }
}
