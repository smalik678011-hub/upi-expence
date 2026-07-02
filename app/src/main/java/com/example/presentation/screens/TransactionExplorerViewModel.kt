package com.example.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.ResultWrapper
import com.example.core.utils.IndianFormattingUtils
import com.example.domain.model.Expense
import com.example.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

enum class SortOption {
    NEWEST_FIRST,
    OLDEST_FIRST,
    HIGHEST_AMOUNT,
    LOWEST_AMOUNT,
    ALPHABETICAL_COUNTERPARTY,
    ALPHABETICAL_APP
}

enum class DateRangeOption {
    ALL,
    TODAY,
    YESTERDAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    THIS_MONTH,
    LAST_MONTH,
    CUSTOM
}

data class TransactionExplorerUiState(
    val isLoading: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val totalCount: Int = 0,
    val totalAmount: Double = 0.0,
    val query: String = "",
    val dateRangeOption: DateRangeOption = DateRangeOption.ALL,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val selectedSourceApps: Set<String> = emptySet(),
    val selectedTypes: Set<String> = emptySet(), // "DEBIT", "CREDIT"
    val selectedStatuses: Set<String> = emptySet(), // "SUCCESS", "PENDING", "FAILED"
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val sortOption: SortOption = SortOption.NEWEST_FIRST,
    val error: String? = null,
    val allAvailableApps: List<String> = emptyList()
)

class TransactionExplorerViewModel(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _dateRangeOption = MutableStateFlow(DateRangeOption.ALL)
    private val _customStartDate = MutableStateFlow<Long?>(null)
    private val _customEndDate = MutableStateFlow<Long?>(null)
    private val _selectedSourceApps = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedTypes = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedStatuses = MutableStateFlow<Set<String>>(emptySet())
    private val _minAmount = MutableStateFlow<Double?>(null)
    private val _maxAmount = MutableStateFlow<Double?>(null)
    private val _sortOption = MutableStateFlow(SortOption.NEWEST_FIRST)

    val query: StateFlow<String> = _query.asStateFlow()
    val dateRangeOption: StateFlow<DateRangeOption> = _dateRangeOption.asStateFlow()
    val customStartDate: StateFlow<Long?> = _customStartDate.asStateFlow()
    val customEndDate: StateFlow<Long?> = _customEndDate.asStateFlow()
    val selectedSourceApps: StateFlow<Set<String>> = _selectedSourceApps.asStateFlow()
    val selectedTypes: StateFlow<Set<String>> = _selectedTypes.asStateFlow()
    val selectedStatuses: StateFlow<Set<String>> = _selectedStatuses.asStateFlow()
    val minAmount: StateFlow<Double?> = _minAmount.asStateFlow()
    val maxAmount: StateFlow<Double?> = _maxAmount.asStateFlow()
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // Create a debounced query flow
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private val debouncedQuery = _query
        .debounce(300)
        .distinctUntilChanged()

    // Expose dynamic apps list based on loaded DB items
    private val _allApps = MutableStateFlow<List<String>>(emptyList())

    val uiState: StateFlow<TransactionExplorerUiState> = combine(
        expenseRepository.getExpenses(),
        debouncedQuery,
        _dateRangeOption,
        _customStartDate,
        _customEndDate,
        _selectedSourceApps,
        _selectedTypes,
        _selectedStatuses,
        _minAmount,
        _maxAmount,
        _sortOption
    ) { flows ->
        val expensesResult = flows[0] as ResultWrapper<List<Expense>>
        val currentQuery = flows[1] as String
        val rangeOption = flows[2] as DateRangeOption
        val customStart = flows[3] as Long?
        val customEnd = flows[4] as Long?
        val sourceApps = flows[5] as Set<String>
        val types = flows[6] as Set<String>
        val statuses = flows[7] as Set<String>
        val minAmt = flows[8] as Double?
        val maxAmt = flows[9] as Double?
        val sortOpt = flows[10] as SortOption

        when (expensesResult) {
            is ResultWrapper.Error -> {
                TransactionExplorerUiState(error = expensesResult.error.message)
            }
            is ResultWrapper.Success -> {
                val rawList = expensesResult.data
                
                // Update available apps dynamically
                val uniqueApps = rawList.mapNotNull { it.accountOrBank }.distinct().sorted()
                _allApps.value = uniqueApps

                // Apply filters
                val filtered = rawList.filter { expense ->
                    // 1. Search Query
                    val matchesSearch = if (currentQuery.isBlank()) {
                        true
                    } else {
                        val normQuery = currentQuery.lowercase().replace("\\s+".toRegex(), "")
                        val merchantNorm = expense.merchantName.lowercase().replace("\\s+".toRegex(), "")
                        val appNorm = (expense.accountOrBank ?: "").lowercase().replace("\\s+".toRegex(), "")
                        val refNorm = (expense.transactionRef ?: "").lowercase().replace("\\s+".toRegex(), "")
                        val amountStr = expense.amount.toString()
                        val formattedDate = IndianFormattingUtils.formatIndianDate(expense.date).lowercase()
                        val rawSmsNorm = (expense.rawSmsBody ?: "").lowercase().replace("\\s+".toRegex(), "")

                        merchantNorm.contains(normQuery) ||
                                appNorm.contains(normQuery) ||
                                refNorm.contains(normQuery) ||
                                amountStr.contains(normQuery) ||
                                formattedDate.contains(normQuery) ||
                                rawSmsNorm.contains(normQuery)
                    }

                    // 2. Date Range
                    val matchesDate = checkDateRange(expense.date.time, rangeOption, customStart, customEnd)

                    // 3. Source Apps
                    val matchesApp = if (sourceApps.isEmpty()) true else {
                        expense.accountOrBank != null && sourceApps.contains(expense.accountOrBank)
                    }

                    // 4. Transaction Type (DEBIT/CREDIT)
                    val matchesType = if (types.isEmpty()) true else {
                        types.contains(expense.uType)
                    }

                    // 5. Transaction Status
                    val matchesStatus = if (statuses.isEmpty()) true else {
                        statuses.contains(expense.status)
                    }

                    // 6. Amount Range
                    val matchesMinAmt = if (minAmt == null) true else expense.amount >= minAmt
                    val matchesMaxAmt = if (maxAmt == null) true else expense.amount <= maxAmt

                    matchesSearch && matchesDate && matchesApp && matchesType && matchesStatus && matchesMinAmt && matchesMaxAmt
                }

                // Apply Sort
                val sorted = when (sortOpt) {
                    SortOption.NEWEST_FIRST -> filtered.sortedByDescending { it.date.time }
                    SortOption.OLDEST_FIRST -> filtered.sortedBy { it.date.time }
                    SortOption.HIGHEST_AMOUNT -> filtered.sortedByDescending { it.amount }
                    SortOption.LOWEST_AMOUNT -> filtered.sortedBy { it.amount }
                    SortOption.ALPHABETICAL_COUNTERPARTY -> filtered.sortedBy { it.merchantName.lowercase() }
                    SortOption.ALPHABETICAL_APP -> filtered.sortedBy { (it.accountOrBank ?: "").lowercase() }
                }

                val totalCount = sorted.size
                val totalAmount = sorted.sumOf { it.amount }

                TransactionExplorerUiState(
                    isLoading = false,
                    expenses = sorted,
                    totalCount = totalCount,
                    totalAmount = totalAmount,
                    query = currentQuery,
                    dateRangeOption = rangeOption,
                    customStartDate = customStart,
                    customEndDate = customEnd,
                    selectedSourceApps = sourceApps,
                    selectedTypes = types,
                    selectedStatuses = statuses,
                    minAmount = minAmt,
                    maxAmount = maxAmt,
                    sortOption = sortOpt,
                    allAvailableApps = uniqueApps
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionExplorerUiState(isLoading = true)
    )

    fun onSearchQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun onDateRangeOptionChanged(option: DateRangeOption) {
        _dateRangeOption.value = option
    }

    fun onCustomDateRangeChanged(start: Long?, end: Long?) {
        _customStartDate.value = start
        _customEndDate.value = end
    }

    fun toggleSourceApp(app: String) {
        val current = _selectedSourceApps.value
        _selectedSourceApps.value = if (current.contains(app)) {
            current - app
        } else {
            current + app
        }
    }

    fun toggleType(type: String) {
        val current = _selectedTypes.value
        _selectedTypes.value = if (current.contains(type)) {
            current - type
        } else {
            current + type
        }
    }

    fun toggleStatus(status: String) {
        val current = _selectedStatuses.value
        _selectedStatuses.value = if (current.contains(status)) {
            current - status
        } else {
            current + status
        }
    }

    fun onAmountRangeChanged(min: Double?, max: Double?) {
        _minAmount.value = min
        _maxAmount.value = max
    }

    fun onSortOptionChanged(option: SortOption) {
        _sortOption.value = option
    }

    fun resetFilters() {
        _dateRangeOption.value = DateRangeOption.ALL
        _customStartDate.value = null
        _customEndDate.value = null
        _selectedSourceApps.value = emptySet()
        _selectedTypes.value = emptySet()
        _selectedStatuses.value = emptySet()
        _minAmount.value = null
        _maxAmount.value = null
        _sortOption.value = SortOption.NEWEST_FIRST
    }

    private fun checkDateRange(
        timestamp: Long,
        option: DateRangeOption,
        customStart: Long?,
        customEnd: Long?
    ): Boolean {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = now

        return when (option) {
            DateRangeOption.ALL -> true
            DateRangeOption.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfToday = calendar.timeInMillis
                
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfToday = calendar.timeInMillis
                
                timestamp in startOfToday..endOfToday
            }
            DateRangeOption.YESTERDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfYesterday = calendar.timeInMillis

                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfYesterday = calendar.timeInMillis

                timestamp in startOfYesterday..endOfYesterday
            }
            DateRangeOption.LAST_7_DAYS -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val sevenDaysAgo = calendar.timeInMillis
                timestamp in sevenDaysAgo..now
            }
            DateRangeOption.LAST_30_DAYS -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val thirtyDaysAgo = calendar.timeInMillis
                timestamp in thirtyDaysAgo..now
            }
            DateRangeOption.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                timestamp in startOfMonth..now
            }
            DateRangeOption.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfLastMonth = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfLastMonth = calendar.timeInMillis

                timestamp in startOfLastMonth..endOfLastMonth
            }
            DateRangeOption.CUSTOM -> {
                val start = customStart ?: 0L
                val end = customEnd ?: Long.MAX_VALUE
                timestamp in start..end
            }
        }
    }
}
