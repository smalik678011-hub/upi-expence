package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.utils.IndianFormattingUtils
import com.example.domain.model.Expense
import com.example.domain.model.NotificationSource
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    adManager: com.example.core.admob.AdManager,
    adRepository: com.example.domain.repository.AdRepository,
    onNavigateToDashboard: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToExplorer: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "UPI Expense Tracker",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                },
                actions = {
                    // Analytics Button
                    IconButton(
                        onClick = onNavigateToAnalytics,
                        modifier = Modifier
                            .testTag("nav_analytics_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Analytics & Insights",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Privacy Pledge Lock Icon
                    IconButton(
                        onClick = onNavigateToPrivacy,
                        modifier = Modifier
                            .testTag("nav_privacy_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Privacy Pledge",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Sandbox Settings icon to navigate to the previous Module 1-5 sandbox
                    IconButton(
                        onClick = onNavigateToDashboard,
                        modifier = Modifier
                            .testTag("nav_dashboard_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "System Sandbox Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            com.example.presentation.components.AdBanner(
                adManager = adManager,
                adRepository = adRepository,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Static Header elements (Search, Filters, Charts slot, Summary Card) 
            // inside a LazyColumn to keep scrollability high and fast on low-end devices.
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is HomeUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                                .testTag("error_state_view"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error icon",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Error Loading Transactions",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                HomeUiState.Empty -> {
                    EmptyHomeScreenContent(onInsertMock = {
                        viewModel.insertMockExpense(getMockExpenses()[0])
                    }, onInsertAllMocks = {
                        getMockExpenses().forEach { viewModel.insertMockExpense(it) }
                    })
                }
                is HomeUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("transaction_list_lazycolumn"),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Sleek Search Bar Placeholder
                        item {
                            SearchBarPlaceholder(onClick = onNavigateToExplorer)
                        }

                        // 2. Summary Card
                        item {
                            SummaryCard(
                                totalSent = state.totalSent,
                                totalReceived = state.totalReceived,
                                netBalance = state.netBalance
                            )
                        }

                        // 3. Category & Analytics Quick Slot (Future Compatibility)
                        item {
                            QuickAnalyticsAndCategoriesSlot()
                        }

                        // 4. Quick Filters Row (Future Compatibility)
                        item {
                            FilterChipsRow()
                        }

                        // 5. Recent Transactions Header with "Clear All" for testing ease
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Transactions",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                
                                TextButton(
                                    onClick = { viewModel.clearAllTransactions() },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Clear All Transactions",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Clear All",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        // 6. Scrollable List of Transaction Cards
                        items(
                            items = state.expenses,
                            key = { it.id }
                        ) { expense ->
                            TransactionCard(
                                expense = expense,
                                onDelete = { viewModel.deleteTransaction(expense.id) },
                                onCardClick = { onNavigateToDetails(expense.id) }
                            )
                        }

                        // 7. sponsored AdMob banner placeholder
                        item {
                            AdMobBannerPlaceholder()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBarPlaceholder(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(100.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search Icon Placeholder",
            tint = MaterialTheme.colorScheme.outline
        )
        Text(
            text = "Search transactions... (Future Search)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = "Filter Icon Placeholder",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SummaryCard(
    totalSent: Double,
    totalReceived: Double,
    netBalance: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("summary_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Net Balance Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = IndianFormattingUtils.formatIndianCurrency(netBalance),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Balance status indicator badge
                val (badgeColor, badgeTextColor, badgeLabel) = if (netBalance >= 0) {
                    Triple(Color(0xFFECFDF5), Color(0xFF047857), "Surplus")
                } else {
                    Triple(Color(0xFFFFF1F2), Color(0xFFBE123C), "Deficit")
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(badgeColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = badgeLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = badgeTextColor
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            // Sent vs Received breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sent Column
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF1F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Sent",
                            tint = Color(0xFFBE123C),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Total Sent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        Text(
                            text = IndianFormattingUtils.formatIndianCurrency(totalSent),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFBE123C)
                        )
                    }
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
                        .align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Received Column
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECFDF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Received",
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Total Received",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        Text(
                            text = IndianFormattingUtils.formatIndianCurrency(totalReceived),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF047857)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAnalyticsAndCategoriesSlot() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Report",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Monthly Spending Report",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "FUTURE MOD",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Auto-generated charts, breakdown by bank/app categories, and localized insights will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun FilterChipsRow() {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            listOf("All Transactions", "Debits (Sent)", "Credits (Received)", "GPay only", "PhonePe only", "Salary", "Food & Dining")
        ) { chipText ->
            val isSelected = chipText == "All Transactions"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                    .clickable(enabled = false) {}
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = chipText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TransactionCard(
    expense: Expense,
    onDelete: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDebit = expense.uType.equals("DEBIT", ignoreCase = true)
    
    // Map UPI package name/type to aesthetic circular brand color
    val (appColor, appInitial) = when {
        expense.rawSmsBody?.contains("gpay", ignoreCase = true) == true || expense.id.contains("gpay") -> {
            Pair(Color(0xFF1A73E8), "G")
        }
        expense.rawSmsBody?.contains("phonepe", ignoreCase = true) == true || expense.id.contains("phonepe") -> {
            Pair(Color(0xFF5F259F), "P")
        }
        expense.rawSmsBody?.contains("paytm", ignoreCase = true) == true || expense.id.contains("paytm") -> {
            Pair(Color(0xFF00B9F1), "Py")
        }
        else -> {
            Pair(MaterialTheme.colorScheme.secondary, expense.merchantName.take(1).uppercase())
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onCardClick)
            .testTag("transaction_item_card_${expense.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Source App/Brand Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(appColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appInitial,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = appColor
                )
            }

            // Transaction Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = expense.merchantName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Transaction Amount
                    Text(
                        text = (if (isDebit) "-" else "+") + IndianFormattingUtils.formatIndianCurrency(expense.amount),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (isDebit) Color(0xFFBE123C) else Color(0xFF047857)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date & Time
                    Text(
                        text = IndianFormattingUtils.formatIndianDate(expense.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    // Transaction Type indicator badge & Delete icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isDebit) Color(0xFFFFF1F2) else Color(0xFFECFDF5)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isDebit) "SENT" else "RECEIVED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (isDebit) Color(0xFFBE123C) else Color(0xFF047857)
                            )
                        }

                        // Compact Delete trigger for easy sandbox manipulation
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete Transaction",
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .clickable { onDelete() }
                        )
                    }
                }
                
                // If ref ID is present, show it compactly
                if (!expense.transactionRef.isNullOrBlank()) {
                    Text(
                        text = "Ref: ${expense.transactionRef}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyHomeScreenContent(
    onInsertMock: () -> Unit,
    onInsertAllMocks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("empty_state_view"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "No notifications illustration",
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Your UPI Ledger is Empty",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No offline transactions are captured yet. Grant the notification listener settings, or click below to populate mock transactions immediately for sandbox demonstration.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onInsertMock,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Add One Mock")
            }

            Button(
                onClick = onInsertAllMocks,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Load All Mocks")
            }
        }
    }
}

@Composable
fun AdMobBannerPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "SPONSORED ADVERTISEMENT",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = "AdMob Banner Placeholder (Future Module Integration)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f)
        )
    }
}

// Generate premium mock transaction data for sandbox demo
private fun getMockExpenses(): List<Expense> {
    return listOf(
        Expense(
            id = "mock_gpay_1",
            amount = 450.0,
            merchantName = "Starbucks Coffee",
            uType = "DEBIT",
            date = Date(System.currentTimeMillis() - 1000 * 60 * 30), // 30 mins ago
            transactionRef = "319245109432",
            accountOrBank = "HDFC Bank",
            rawSmsBody = "gpay coffee transaction completed"
        ),
        Expense(
            id = "mock_phonepe_2",
            amount = 12500.0,
            merchantName = "Aman Sharma (Refund)",
            uType = "CREDIT",
            date = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 3), // 3 hours ago
            transactionRef = "319245109455",
            accountOrBank = "ICICI Bank",
            rawSmsBody = "phonepe transaction refund"
        ),
        Expense(
            id = "mock_paytm_3",
            amount = 120.0,
            merchantName = "Sharma Ji Grocery Kirana",
            uType = "DEBIT",
            date = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 18), // 18 hours ago
            transactionRef = "319245109489",
            accountOrBank = "SBI",
            rawSmsBody = "paytm grocery payment successful"
        ),
        Expense(
            id = "mock_gpay_4",
            amount = 2500.0,
            merchantName = "Hindustan Petroleum",
            uType = "DEBIT",
            date = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 36), // 1.5 days ago
            transactionRef = "319245109501",
            accountOrBank = "HDFC Bank",
            rawSmsBody = "gpay successful fuel payment"
        ),
        Expense(
            id = "mock_phonepe_5",
            amount = 45000.0,
            merchantName = "Salary Credited",
            uType = "CREDIT",
            date = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 48), // 2 days ago
            transactionRef = "319245109520",
            accountOrBank = "SBI",
            rawSmsBody = "salary phonepe transfer"
        )
    )
}
