package com.example.presentation.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.utils.IndianFormattingUtils
import com.example.domain.model.Expense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionExplorerScreen(
    viewModel: TransactionExplorerViewModel,
    onNavigateToDetails: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rawQuery by viewModel.query.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var showFilterSheet by remember { mutableStateOf(false) }

    // Count active filters
    val activeFiltersCount = remember(uiState) {
        var count = 0
        if (uiState.dateRangeOption != DateRangeOption.ALL) count++
        if (uiState.selectedSourceApps.isNotEmpty()) count++
        if (uiState.selectedTypes.isNotEmpty()) count++
        if (uiState.selectedStatuses.isNotEmpty()) count++
        if (uiState.minAmount != null || uiState.maxAmount != null) count++
        if (uiState.sortOption != SortOption.NEWEST_FIRST) count++
        count
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transaction Explorer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.testTag("explorer_title")
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_to_home")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.resetFilters() },
                        modifier = Modifier.testTag("btn_reset_all_filters")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Filters"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input Block
            OutlinedTextField(
                value = rawQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_input"),
                placeholder = { Text("Search counterparty, reference, amount...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (rawQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onSearchQueryChanged("") },
                            modifier = Modifier.testTag("btn_clear_search")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Filtering Quick Access Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .testTag("btn_all_filters"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeFiltersCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (activeFiltersCount > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (activeFiltersCount > 0) "Filters ($activeFiltersCount)" else "All Filters",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                FilledTonalButton(
                    onClick = {
                        val nextSort = when (uiState.sortOption) {
                            SortOption.NEWEST_FIRST -> SortOption.HIGHEST_AMOUNT
                            SortOption.HIGHEST_AMOUNT -> SortOption.LOWEST_AMOUNT
                            SortOption.LOWEST_AMOUNT -> SortOption.ALPHABETICAL_COUNTERPARTY
                            else -> SortOption.NEWEST_FIRST
                        }
                        viewModel.onSortOptionChanged(nextSort)
                    },
                    modifier = Modifier.testTag("btn_cycle_sort"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (uiState.sortOption) {
                            SortOption.NEWEST_FIRST -> "Newest"
                            SortOption.OLDEST_FIRST -> "Oldest"
                            SortOption.HIGHEST_AMOUNT -> "Highest"
                            SortOption.LOWEST_AMOUNT -> "Lowest"
                            SortOption.ALPHABETICAL_COUNTERPARTY -> "Name A-Z"
                            SortOption.ALPHABETICAL_APP -> "App A-Z"
                        },
                        fontSize = 13.sp
                    )
                }
            }

            // Stats Aggregator Bar
            if (!uiState.isLoading && uiState.error == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("explorer_stats_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FILTERED SUMMARY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${uiState.totalCount} transaction(s)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.testTag("stats_count_label")
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TOTAL VALUE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = IndianFormattingUtils.formatIndianCurrency(uiState.totalAmount),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("stats_amount_label")
                            )
                        }
                    }
                }
            }

            // Main List or Error/Loading
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.testTag("explorer_loading"))
                    }
                } else if (uiState.error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Unknown error occurred",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.testTag("explorer_error_text")
                        )
                    }
                } else if (uiState.expenses.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No transactions found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("explorer_empty_title")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search keywords or active filters.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("explorer_empty_desc")
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("explorer_transaction_list"),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(
                            items = uiState.expenses,
                            key = { it.id }
                        ) { expense ->
                            TransactionListItem(
                                expense = expense,
                                onClick = { onNavigateToDetails(expense.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Comprehensive Filter Modal Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = Modifier.testTag("filters_bottom_sheet")
        ) {
            FilterPanelContent(
                uiState = uiState,
                viewModel = viewModel,
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}

@Composable
fun TransactionListItem(
    expense: Expense,
    onClick: () -> Unit
) {
    val isDebit = expense.uType.equals("DEBIT", ignoreCase = true)
    val amountColor = if (isDebit) {
        MaterialTheme.colorScheme.error
    } else {
        Color(0xFF2E7D32) // Emerald Green
    }
    val typeIcon = if (isDebit) {
        Icons.Default.ArrowUpward
    } else {
        Icons.Default.ArrowDownward
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("transaction_item_${expense.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Direction Arrow Indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(amountColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = if (isDebit) "Sent" else "Received",
                    tint = amountColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = expense.merchantName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = (if (isDebit) "-" else "+") + IndianFormattingUtils.formatIndianCurrency(expense.amount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = amountColor,
                        modifier = Modifier.testTag("item_amount_${expense.id}")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = IndianFormattingUtils.formatIndianDate(expense.date),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Source app indicator
                    Text(
                        text = expense.accountOrBank ?: "Unknown App",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (!expense.transactionRef.isNullOrBlank() || expense.status != "SUCCESS") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (!expense.transactionRef.isNullOrBlank()) "Ref: ${expense.transactionRef}" else "",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Status badge if failure or pending
                        if (expense.status != "SUCCESS") {
                            val statusBgColor = when (expense.status) {
                                "PENDING" -> Color(0xFFFFF3E0) // Light Amber
                                "FAILED" -> Color(0xFFFFEBEE) // Light Red
                                else -> Color(0xFFF5F5F5)
                            }
                            val statusTextColor = when (expense.status) {
                                "PENDING" -> Color(0xFFE65100)
                                "FAILED" -> Color(0xFFC62828)
                                else -> Color(0xFF616161)
                            }

                            Text(
                                text = expense.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusTextColor,
                                modifier = Modifier
                                    .background(statusBgColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("item_status_${expense.id}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterPanelContent(
    uiState: TransactionExplorerUiState,
    viewModel: TransactionExplorerViewModel,
    onDismiss: () -> Unit
) {
    var minAmtText by remember { mutableStateOf(uiState.minAmount?.toString() ?: "") }
    var maxAmtText by remember { mutableStateOf(uiState.maxAmount?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp)
            .testTag("filter_panel_container")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Refine Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(
                onClick = {
                    viewModel.resetFilters()
                    minAmtText = ""
                    maxAmtText = ""
                },
                modifier = Modifier.testTag("btn_panel_reset")
            ) {
                Text("Clear All")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
        ) {
            // 1. Transaction Direction / Type
            item {
                Text(
                    text = "Transaction Type",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = uiState.selectedTypes.contains("DEBIT"),
                        onClick = { viewModel.toggleType("DEBIT") },
                        label = { Text("Sent / Debit") },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("chip_type_debit")
                    )
                    FilterChip(
                        selected = uiState.selectedTypes.contains("CREDIT"),
                        onClick = { viewModel.toggleType("CREDIT") },
                        label = { Text("Received / Credit") },
                        modifier = Modifier.testTag("chip_type_credit")
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. Date Ranges
            item {
                Text(
                    text = "Time Period",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateRangeOption.values().forEach { option ->
                        FilterChip(
                            selected = uiState.dateRangeOption == option,
                            onClick = { viewModel.onDateRangeOptionChanged(option) },
                            label = {
                                Text(
                                    when (option) {
                                        DateRangeOption.ALL -> "All Time"
                                        DateRangeOption.TODAY -> "Today"
                                        DateRangeOption.YESTERDAY -> "Yesterday"
                                        DateRangeOption.LAST_7_DAYS -> "Last 7 Days"
                                        DateRangeOption.LAST_30_DAYS -> "Last 30 Days"
                                        DateRangeOption.THIS_MONTH -> "This Month"
                                        DateRangeOption.LAST_MONTH -> "Last Month"
                                        DateRangeOption.CUSTOM -> "Custom Range"
                                    }
                                )
                            },
                            modifier = Modifier.testTag("chip_date_${option.name}")
                        )
                    }
                }

                if (uiState.dateRangeOption == DateRangeOption.CUSTOM) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Supports custom filters on dates (Simulation defaults to entire range)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Amount Range
            item {
                Text(
                    text = "Amount Range (₹)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minAmtText,
                        onValueChange = {
                            minAmtText = it
                            val minVal = it.toDoubleOrNull()
                            val maxVal = maxAmtText.toDoubleOrNull()
                            viewModel.onAmountRangeChanged(minVal, maxVal)
                        },
                        label = { Text("Min Amount") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_min_amount"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxAmtText,
                        onValueChange = {
                            maxAmtText = it
                            val minVal = minAmtText.toDoubleOrNull()
                            val maxVal = it.toDoubleOrNull()
                            viewModel.onAmountRangeChanged(minVal, maxVal)
                        },
                        label = { Text("Max Amount") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_max_amount"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 4. Source Application
            if (uiState.allAvailableApps.isNotEmpty()) {
                item {
                    Text(
                        text = "Source Applications",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.allAvailableApps.forEach { app ->
                            FilterChip(
                                selected = uiState.selectedSourceApps.contains(app),
                                onClick = { viewModel.toggleSourceApp(app) },
                                label = { Text(app) },
                                modifier = Modifier.testTag("chip_app_$app")
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 5. Transaction Status
            item {
                Text(
                    text = "Transaction Status",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = uiState.selectedStatuses.contains("SUCCESS"),
                        onClick = { viewModel.toggleStatus("SUCCESS") },
                        label = { Text("Success") },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("chip_status_success")
                    )
                    FilterChip(
                        selected = uiState.selectedStatuses.contains("PENDING"),
                        onClick = { viewModel.toggleStatus("PENDING") },
                        label = { Text("Pending") },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("chip_status_pending")
                    )
                    FilterChip(
                        selected = uiState.selectedStatuses.contains("FAILED"),
                        onClick = { viewModel.toggleStatus("FAILED") },
                        label = { Text("Failed") },
                        modifier = Modifier.testTag("chip_status_failed")
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_apply_filters"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Apply & View Results",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}
