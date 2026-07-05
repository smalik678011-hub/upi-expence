package com.example.data.repository

import com.example.core.error.ResultWrapper
import com.example.data.database.dao.ExpenseDao
import com.example.data.database.dao.ExpenseAnalyticsProjection
import com.example.domain.model.*
import com.example.domain.repository.AnalyticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsRepositoryImpl(
    private val expenseDao: ExpenseDao
) : AnalyticsRepository {

    override suspend fun getAnalytics(
        filters: AnalyticsFilters
    ): ResultWrapper<AnalyticsData> = withContext(Dispatchers.IO) {
        try {
            val startDateLong = filters.startDate?.time ?: 0L
            val endDateLong = filters.endDate?.time ?: 0L
            val minAmountVal = filters.minAmount ?: 0.0
            val maxAmountVal = filters.maxAmount ?: 0.0

            // Query only the thin projection matching the filters
            val rawData = expenseDao.getAnalyticsProjectionFiltered(
                startDate = startDateLong,
                endDate = endDateLong,
                sourceApp = filters.sourceApp,
                uType = filters.transactionType,
                minAmount = minAmountVal,
                maxAmount = maxAmountVal
            )

            if (rawData.isEmpty()) {
                return@withContext ResultWrapper.Success(createEmptyAnalyticsData())
            }

            val now = Calendar.getInstance()
            val todayStart = getStartOfDay(now.timeInMillis)
            
            val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yesterdayStart = getStartOfDay(yesterdayCal.timeInMillis)
            val yesterdayEnd = getEndOfDay(yesterdayCal.timeInMillis)

            val last7DaysStart = getStartOfDay(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000)
            val last30DaysStart = getStartOfDay(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)

            val thisMonthStart = getStartOfMonth(now.timeInMillis)
            val lastMonthStart = getStartOfPreviousMonth(now.timeInMillis)
            val lastMonthEnd = getEndOfPreviousMonth(now.timeInMillis)

            val thisYearStart = getStartOfYear(now.timeInMillis)

            // Lifetime bounds (from the queried data itself, or database in general)
            var todaySpend = 0.0
            var yesterdaySpend = 0.0
            var last7DaysSpend = 0.0
            var last30DaysSpend = 0.0
            var thisMonthSpend = 0.0
            var lastMonthSpend = 0.0
            var thisYearSpend = 0.0
            var lifetimeSpent = 0.0
            var lifetimeReceived = 0.0
            var highestSingleExpense = 0.0
            var highestSingleCredit = 0.0
            var debitCount = 0
            var creditCount = 0

            // Helper for finding time spans
            var minDateLong = Long.MAX_VALUE
            var maxDateLong = Long.MIN_VALUE

            // Grouping containers
            val dailyMap = mutableMapOf<String, Double>() // "yyyy-MM-dd" -> Debit sum
            val weekdayMap = mutableMapOf<Int, Double>() // Calendar.DAY_OF_WEEK -> Debit sum
            val appUsageCount = mutableMapOf<String, Int>() // AppName -> Transaction count
            val counterpartySpend = mutableMapOf<String, Double>() // Counterparty -> Spend sum
            val counterpartyCount = mutableMapOf<String, Int>() // Counterparty -> Count

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            for (item in rawData) {
                val isDebit = item.uType == "DEBIT"
                val amount = item.amount
                val tDate = item.dateLong

                if (tDate < minDateLong) minDateLong = tDate
                if (tDate > maxDateLong) maxDateLong = tDate

                if (isDebit) {
                    lifetimeSpent += amount
                    debitCount++
                    if (amount > highestSingleExpense) {
                        highestSingleExpense = amount
                    }

                    // Spend aggregations by dates
                    if (tDate >= todayStart) {
                        todaySpend += amount
                    }
                    if (tDate in yesterdayStart..yesterdayEnd) {
                        yesterdaySpend += amount
                    }
                    if (tDate >= last7DaysStart) {
                        last7DaysSpend += amount
                    }
                    if (tDate >= last30DaysStart) {
                        last30DaysSpend += amount
                    }
                    if (tDate >= thisMonthStart) {
                        thisMonthSpend += amount
                    }
                    if (tDate in lastMonthStart..lastMonthEnd) {
                        lastMonthSpend += amount
                    }
                    if (tDate >= thisYearStart) {
                        thisYearSpend += amount
                    }

                    // Daily trends
                    val dayStr = dateFormat.format(Date(tDate))
                    dailyMap[dayStr] = (dailyMap[dayStr] ?: 0.0) + amount

                    // Weekdays
                    val cal = Calendar.getInstance().apply { timeInMillis = tDate }
                    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    weekdayMap[dayOfWeek] = (weekdayMap[dayOfWeek] ?: 0.0) + amount

                    // Counterparties
                    val merchant = item.merchantName.trim()
                    if (merchant.isNotEmpty()) {
                        counterpartySpend[merchant] = (counterpartySpend[merchant] ?: 0.0) + amount
                        counterpartyCount[merchant] = (counterpartyCount[merchant] ?: 0) + 1
                    }
                } else {
                    lifetimeReceived += amount
                    creditCount++
                    if (amount > highestSingleCredit) {
                        highestSingleCredit = amount
                    }
                }

                // UPI App stats
                val app = item.accountOrBank ?: "Unknown App"
                appUsageCount[app] = (appUsageCount[app] ?: 0) + 1
            }

            val netBalance = lifetimeReceived - lifetimeSpent
            val totalCount = rawData.size

            // Days / Months calculation
            val totalDays = if (minDateLong < Long.MAX_VALUE && maxDateLong > Long.MIN_VALUE) {
                val diffMs = Math.max(86400000L, System.currentTimeMillis() - minDateLong)
                (diffMs / (1000L * 60 * 60 * 24)).coerceAtLeast(1)
            } else {
                1
            }

            val totalMonths = if (minDateLong < Long.MAX_VALUE && maxDateLong > Long.MIN_VALUE) {
                val diffMs = Math.max(86400000L, System.currentTimeMillis() - minDateLong)
                (diffMs / (1000L * 60 * 60 * 24 * 30)).coerceAtLeast(1)
            } else {
                1
            }

            val averageDailySpend = lifetimeSpent / totalDays
            val averageMonthlySpend = lifetimeSpent / totalMonths

            // --- CHART DATA GENERATION ---

            // 1. Daily spending trend: last 7 days
            val dailyTrendList = mutableListOf<ChartDataPoint>()
            val trendFormat = SimpleDateFormat("dd MMM", Locale.US)
            for (i in 6 downTo 0) {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                val dayKey = dateFormat.format(cal.time)
                val dayLabel = trendFormat.format(cal.time)
                dailyTrendList.add(ChartDataPoint(dayLabel, dailyMap[dayKey] ?: 0.0))
            }

            // 2. Weekly spending trend: last 4 weeks
            val weeklyTrendList = mutableListOf<ChartDataPoint>()
            for (i in 3 downTo 0) {
                val weekStartMs = System.currentTimeMillis() - (i + 1) * 7L * 24 * 60 * 60 * 1000
                val weekEndMs = System.currentTimeMillis() - i * 7L * 24 * 60 * 60 * 1000
                val weeklySum = rawData.filter { it.uType == "DEBIT" && it.dateLong in weekStartMs until weekEndMs }
                    .sumOf { it.amount }
                val weekLabel = if (i == 0) "This Week" else "$i wk ago"
                weeklyTrendList.add(ChartDataPoint(weekLabel, weeklySum))
            }

            // 3. Monthly spending trend: last 6 months
            val monthlyTrendList = mutableListOf<ChartDataPoint>()
            val monthFormat = SimpleDateFormat("MMM", Locale.US)
            for (i in 5 downTo 0) {
                val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
                val mStart = getStartOfMonth(cal.timeInMillis)
                val mEnd = getEndOfMonth(cal.timeInMillis)
                val monthlySum = rawData.filter { it.uType == "DEBIT" && it.dateLong in mStart..mEnd }
                    .sumOf { it.amount }
                monthlyTrendList.add(ChartDataPoint(monthFormat.format(cal.time), monthlySum))
            }

            // 4. Transactions by UPI App (Top 5 count)
            val upiAppUsageList = appUsageCount.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { ChartDataPoint(it.key, it.value.toDouble()) }

            // 5. Top Counterparties (Top 5 spent)
            val topCounterpartiesList = counterpartySpend.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { ChartDataPoint(it.key, it.value) }

            // --- INSIGHTS GENERATION ---
            val insightsList = mutableListOf<Insight>()

            // A. Most used UPI App
            val topAppEntry = appUsageCount.maxByOrNull { it.value }
            if (topAppEntry != null) {
                insightsList.add(
                    Insight(
                        title = "Most Used UPI App",
                        value = topAppEntry.key,
                        description = "Preferred app, used for ${topAppEntry.value} transaction${if (topAppEntry.value > 1) "s" else ""}.",
                        type = InsightType.SUCCESS
                    )
                )
            }

            // B. Most Frequent Counterparty
            val topCounterpartyEntry = counterpartyCount.maxByOrNull { it.value }
            if (topCounterpartyEntry != null) {
                val totalSpentWithThem = counterpartySpend[topCounterpartyEntry.key] ?: 0.0
                insightsList.add(
                    Insight(
                        title = "Top Connection",
                        value = topCounterpartyEntry.key,
                        description = "You transacted ${topCounterpartyEntry.value} times, spending ₹${"%,.2f".format(totalSpentWithThem)}.",
                        type = InsightType.HIGHLIGHT
                    )
                )
            }

            // C. Highest Spending Day of the Week
            val weekdayNames = mapOf(
                Calendar.SUNDAY to "Sunday",
                Calendar.MONDAY to "Monday",
                Calendar.TUESDAY to "Tuesday",
                Calendar.WEDNESDAY to "Wednesday",
                Calendar.THURSDAY to "Thursday",
                Calendar.FRIDAY to "Friday",
                Calendar.SATURDAY to "Saturday"
            )
            val topWeekdayEntry = weekdayMap.maxByOrNull { it.value }
            if (topWeekdayEntry != null) {
                insightsList.add(
                    Insight(
                        title = "Peak Spend Day",
                        value = weekdayNames[topWeekdayEntry.key] ?: "Unknown",
                        description = "Accumulated your highest overall spend of ₹${"%,.2f".format(topWeekdayEntry.value)} on this weekday.",
                        type = InsightType.INFO
                    )
                )
            }

            // D. Growth Comparisons
            // Monthly growth
            val monthlyGrowthPercent = if (lastMonthSpend > 0.0) {
                ((thisMonthSpend - lastMonthSpend) / lastMonthSpend) * 100.0
            } else {
                0.0
            }
            if (lastMonthSpend > 0.0) {
                val (direction, type) = if (monthlyGrowthPercent > 0.0) {
                    Pair("increased by", InsightType.WARNING)
                } else {
                    Pair("decreased by", InsightType.SUCCESS)
                }
                insightsList.add(
                    Insight(
                        title = "Monthly Spending Trend",
                        value = "${String.format("%.1f", Math.abs(monthlyGrowthPercent))}%",
                        description = "Your spending this month has $direction compared to last month (₹${"%,.2f".format(lastMonthSpend)}).",
                        type = type
                    )
                )
            }

            // Weekly Growth Comparison (Last 7 days vs previous 7 days)
            val prev7DaysStart = getStartOfDay(System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000)
            val prev7DaysSpend = rawData.filter { it.uType == "DEBIT" && it.dateLong in prev7DaysStart until last7DaysStart }
                .sumOf { it.amount }
            val weeklyGrowthPercent = if (prev7DaysSpend > 0.0) {
                ((last7DaysSpend - prev7DaysSpend) / prev7DaysSpend) * 100.0
            } else {
                0.0
            }
            if (prev7DaysSpend > 0.0) {
                val (direction, type) = if (weeklyGrowthPercent > 0.0) {
                    Pair("higher than", InsightType.WARNING)
                } else {
                    Pair("lower than", InsightType.SUCCESS)
                }
                insightsList.add(
                    Insight(
                        title = "Weekly Velocity",
                        value = "${String.format("%.1f", Math.abs(weeklyGrowthPercent))}%",
                        description = "Your weekly spend is $direction the previous week's spend of ₹${"%,.2f".format(prev7DaysSpend)}.",
                        type = type
                    )
                )
            }

            // E. Average Transaction Value
            val avgTxValue = if (debitCount > 0) lifetimeSpent / debitCount else 0.0
            if (avgTxValue > 0.0) {
                insightsList.add(
                    Insight(
                        title = "Avg Ticket Size",
                        value = "₹${"%,.2f".format(avgTxValue)}",
                        description = "Your average transaction amount across all $debitCount offline payments.",
                        type = InsightType.INFO
                    )
                )
            }

            // F. Largest Transactions
            if (highestSingleExpense > 0.0) {
                insightsList.add(
                    Insight(
                        title = "Peak Debit",
                        value = "₹${"%,.2f".format(highestSingleExpense)}",
                        description = "The largest single expense recorded offline in your logs.",
                        type = InsightType.HIGHLIGHT
                    )
                )
            }

            val analyticsData = AnalyticsData(
                todaySpend = todaySpend,
                yesterdaySpend = yesterdaySpend,
                last7DaysSpend = last7DaysSpend,
                last30DaysSpend = last30DaysSpend,
                thisMonthSpend = thisMonthSpend,
                lastMonthSpend = lastMonthSpend,
                thisYearSpend = thisYearSpend,
                lifetimeSpent = lifetimeSpent,
                lifetimeReceived = lifetimeReceived,
                netBalance = netBalance,
                averageDailySpend = averageDailySpend,
                averageMonthlySpend = averageMonthlySpend,
                highestSingleExpense = highestSingleExpense,
                highestSingleCredit = highestSingleCredit,
                totalTransactions = totalCount,
                insights = insightsList,
                dailyTrend = dailyTrendList,
                weeklyTrend = weeklyTrendList,
                monthlyTrend = monthlyTrendList,
                sentVsReceived = Pair(lifetimeSpent, lifetimeReceived),
                upiAppUsage = upiAppUsageList,
                topCounterparties = topCounterpartiesList
            )

            ResultWrapper.Success(analyticsData)
        } catch (e: Exception) {
            ResultWrapper.Error(
                com.example.core.error.ErrorModel(
                    code = "ANALYTICS_FAILED",
                    message = "Failed to calculate local analytics: ${e.message}",
                    throwable = e
                )
            )
        }
    }

    private fun createEmptyAnalyticsData(): AnalyticsData {
        return AnalyticsData(
            todaySpend = 0.0,
            yesterdaySpend = 0.0,
            last7DaysSpend = 0.0,
            last30DaysSpend = 0.0,
            thisMonthSpend = 0.0,
            lastMonthSpend = 0.0,
            thisYearSpend = 0.0,
            lifetimeSpent = 0.0,
            lifetimeReceived = 0.0,
            netBalance = 0.0,
            averageDailySpend = 0.0,
            averageMonthlySpend = 0.0,
            highestSingleExpense = 0.0,
            highestSingleCredit = 0.0,
            totalTransactions = 0,
            insights = emptyList(),
            dailyTrend = emptyList(),
            weeklyTrend = emptyList(),
            monthlyTrend = emptyList(),
            sentVsReceived = Pair(0.0, 0.0),
            upiAppUsage = emptyList(),
            topCounterparties = emptyList()
        )
    }

    // --- TIME UTILITIES ---
    private fun getStartOfDay(ms: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = ms
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDay(ms: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = ms
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun getStartOfMonth(ms: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = ms
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfMonth(ms: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = ms
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun getStartOfPreviousMonth(ms: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = ms
            add(Calendar.MONTH, -1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfPreviousMonth(ms: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = ms
            add(Calendar.MONTH, -1)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun getStartOfYear(ms: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = ms
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
