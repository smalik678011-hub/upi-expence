package com.example.domain.model

import java.util.Date

data class AnalyticsFilters(
    val startDate: Date? = null,
    val endDate: Date? = null,
    val sourceApp: String? = null,
    val transactionType: String? = null, // "DEBIT", "CREDIT" or null for both
    val minAmount: Double? = null,
    val maxAmount: Double? = null
)

data class AnalyticsData(
    val todaySpend: Double,
    val yesterdaySpend: Double,
    val last7DaysSpend: Double,
    val last30DaysSpend: Double,
    val thisMonthSpend: Double,
    val lastMonthSpend: Double,
    val thisYearSpend: Double,
    val lifetimeSpent: Double,
    val lifetimeReceived: Double,
    val netBalance: Double,
    val averageDailySpend: Double,
    val averageMonthlySpend: Double,
    val highestSingleExpense: Double,
    val highestSingleCredit: Double,
    val totalTransactions: Int,
    
    // Insights
    val insights: List<Insight>,
    
    // Chart Data
    val dailyTrend: List<ChartDataPoint>,
    val weeklyTrend: List<ChartDataPoint>,
    val monthlyTrend: List<ChartDataPoint>,
    val sentVsReceived: Pair<Double, Double>,
    val upiAppUsage: List<ChartDataPoint>,
    val topCounterparties: List<ChartDataPoint>
)

data class Insight(
    val title: String,
    val value: String,
    val description: String,
    val type: InsightType
)

enum class InsightType {
    INFO, SUCCESS, WARNING, HIGHLIGHT
}

data class ChartDataPoint(
    val label: String,
    val value: Double,
    val secondaryValue: Double = 0.0
)
