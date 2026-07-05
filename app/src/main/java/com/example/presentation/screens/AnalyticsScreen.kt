package com.example.presentation.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.utils.IndianFormattingUtils
import com.example.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedChartTab by remember { mutableStateOf(0) } // 0 = Daily, 1 = Weekly, 2 = Monthly

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analytics & Insights",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .testTag("analytics_back_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .testTag("analytics_filter_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        val activeFilterCount = getActiveFiltersCount(uiState.filters)
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge { Text("$activeFilterCount") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter Analytics",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.testTag("analytics_loading_indicator"))
                }
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .testTag("analytics_error_state"),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.loadAnalytics() }) {
                        Text("Retry")
                    }
                }
            } else {
                val data = uiState.data
                if (data == null || data.totalTransactions == 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .testTag("analytics_empty_state"),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "No Data",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No offline transactions match the active filters.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (getActiveFiltersCount(uiState.filters) > 0) {
                            OutlinedButton(onClick = { viewModel.clearFilters() }) {
                                Text("Clear Filters")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("analytics_scroll_content"),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // KPI Highlights Grid
                        item {
                            KpiHighlightsGrid(data)
                        }

                        // Segmented Spend Trend Chart
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Spending Trend",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        // Tab selectors
                                        TabRow(
                                            selectedTabIndex = selectedChartTab,
                                            modifier = Modifier
                                                .width(220.dp)
                                                .height(36.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            indicator = @Composable { Box {} },
                                            divider = {}
                                        ) {
                                            listOf("Daily", "Weekly", "Monthly").forEachIndexed { index, label ->
                                                val selected = selectedChartTab == index
                                                Tab(
                                                    selected = selected,
                                                    onClick = { selectedChartTab = index },
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                                ) {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                                        ),
                                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    val chartData = when (selectedChartTab) {
                                        0 -> data.dailyTrend
                                        1 -> data.weeklyTrend
                                        else -> data.monthlyTrend
                                    }

                                    if (chartData.isNotEmpty()) {
                                        LocalLineChart(chartData)
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("No trend data in selected range.", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }

                        // Deterministic Insights Engine Section
                        if (data.insights.isNotEmpty()) {
                            item {
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Text(
                                        text = "Deterministic spending insights",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        data.insights.forEach { insight ->
                                            InsightCard(insight)
                                        }
                                    }
                                }
                            }
                        }

                        // App Usage and Top Counterparties breakdowns
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (data.upiAppUsage.isNotEmpty()) {
                                    BreakdownCard(
                                        title = "UPI App Wallet Share",
                                        dataPoints = data.upiAppUsage,
                                        isCurrency = false,
                                        icon = Icons.Default.Payment
                                    )
                                }

                                if (data.topCounterparties.isNotEmpty()) {
                                    BreakdownCard(
                                        title = "Top Spending Connections",
                                        dataPoints = data.topCounterparties,
                                        isCurrency = true,
                                        icon = Icons.Default.Handshake
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- FILTER BOTTOM SHEET / DIALOG ---
    if (showFilterSheet) {
        var tempStartDate by remember { mutableStateOf(uiState.filters.startDate) }
        var tempEndDate by remember { mutableStateOf(uiState.filters.endDate) }
        var tempSourceApp by remember { mutableStateOf(uiState.filters.sourceApp) }
        var tempTxType by remember { mutableStateOf(uiState.filters.transactionType) }
        var tempMinAmount by remember { mutableStateOf(uiState.filters.minAmount?.toString() ?: "") }
        var tempMaxAmount by remember { mutableStateOf(uiState.filters.maxAmount?.toString() ?: "") }

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        AlertDialog(
            onDismissRequest = { showFilterSheet = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filter Analytics", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text("Reset All")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date Pickers
                    Text("Time Range", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                tempStartDate?.let { calendar.time = it }
                                DatePickerDialog(context, { _, y, m, d ->
                                    val cal = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }
                                    tempStartDate = cal.time
                                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tempStartDate?.let { dateFormat.format(it) } ?: "Start Date",
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val calendar = Calendar.getInstance()
                                tempEndDate?.let { calendar.time = it }
                                DatePickerDialog(context, { _, y, m, d ->
                                    val cal = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }
                                    tempEndDate = cal.time
                                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tempEndDate?.let { dateFormat.format(it) } ?: "End Date",
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                    }

                    // Transaction Type Segments
                    Text("Transaction Direction", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(null to "All", "DEBIT" to "Debits", "CREDIT" to "Credits").forEach { (type, label) ->
                            val isSelected = tempTxType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { tempTxType = type },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Source Apps Selection
                    Text("Source App", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Column {
                        var expandedAppDropdown by remember { mutableStateOf(false) }
                        OutlinedCard(
                            onClick = { expandedAppDropdown = !expandedAppDropdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tempSourceApp ?: "All Apps")
                                Icon(
                                    imageVector = if (expandedAppDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = expandedAppDropdown,
                            onDismissRequest = { expandedAppDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Apps") },
                                onClick = {
                                    tempSourceApp = null
                                    expandedAppDropdown = false
                                }
                            )
                            uiState.availableSourceApps.forEach { app ->
                                DropdownMenuItem(
                                    text = { Text(app) },
                                    onClick = {
                                        tempSourceApp = app
                                        expandedAppDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Amount Range Fields
                    Text("Amount Range", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tempMinAmount,
                            onValueChange = { tempMinAmount = it },
                            label = { Text("Min (₹)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = tempMaxAmount,
                            onValueChange = { tempMaxAmount = it },
                            label = { Text("Max (₹)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minVal = tempMinAmount.toDoubleOrNull()
                        val maxVal = tempMaxAmount.toDoubleOrNull()
                        viewModel.updateFilters(
                            startDate = tempStartDate,
                            endDate = tempEndDate,
                            sourceApp = tempSourceApp,
                            transactionType = tempTxType,
                            minAmount = minVal,
                            maxAmount = maxVal
                        )
                        showFilterSheet = false
                    },
                    modifier = Modifier.testTag("apply_filter_confirm_btn")
                ) {
                    Text("Apply Filters")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilterSheet = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getActiveFiltersCount(filters: AnalyticsFilters): Int {
    var count = 0
    if (filters.startDate != null) count++
    if (filters.endDate != null) count++
    if (filters.sourceApp != null) count++
    if (filters.transactionType != null) count++
    if (filters.minAmount != null) count++
    if (filters.maxAmount != null) count++
    return count
}

@Composable
fun KpiHighlightsGrid(data: AnalyticsData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Spent This Month",
                value = "₹${"%,.0f".format(data.thisMonthSpend)}",
                subtitle = "Last Mo: ₹${"%,.0f".format(data.lastMonthSpend)}",
                color = MaterialTheme.colorScheme.primaryContainer,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )

            KpiCard(
                title = "Today's Spent",
                value = "₹${"%,.0f".format(data.todaySpend)}",
                subtitle = "Yesterday: ₹${"%,.0f".format(data.yesterdaySpend)}",
                color = MaterialTheme.colorScheme.secondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = "Avg Monthly Spend",
                value = "₹${"%,.0f".format(data.averageMonthlySpend)}",
                subtitle = "Daily: ₹${"%,.0f".format(data.averageDailySpend)}",
                color = MaterialTheme.colorScheme.tertiaryContainer,
                textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.weight(1f)
            )

            val balanceColor = if (data.netBalance >= 0) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
            val balanceTextColor = if (data.netBalance >= 0) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            }
            KpiCard(
                title = "Net Cashflow",
                value = "${if (data.netBalance >= 0) "+" else ""}₹${"%,.0f".format(data.netBalance)}",
                subtitle = "Inflow vs Outflow",
                color = balanceColor,
                textColor = balanceTextColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
                color = textColor.copy(alpha = 0.75f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = textColor,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.65f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Composable
fun InsightCard(insight: Insight) {
    val (bgColor, contentColor, icon) = when (insight.type) {
        InsightType.SUCCESS -> Triple(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary,
            Icons.Default.CheckCircle
        )
        InsightType.WARNING -> Triple(
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.error,
            Icons.Default.Warning
        )
        InsightType.HIGHLIGHT -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.LocalFireDepartment
        )
        InsightType.INFO -> Triple(
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.secondary,
            Icons.Default.Info
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = insight.value,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = contentColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LocalLineChart(dataPoints: List<ChartDataPoint>) {
    val maxValue = dataPoints.maxOf { it.value }.coerceAtLeast(1.0)
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 8.dp)
    ) {
        val width = size.width
        val height = size.height
        val paddingBottom = 24.dp.toPx()
        val paddingTop = 12.dp.toPx()
        val chartHeight = height - paddingBottom - paddingTop
        val numPoints = dataPoints.size
        val xStep = if (numPoints > 1) width / (numPoints - 1) else width

        // Draw horizontal grid lines & text labels
        val gridCount = 4
        for (i in 0..gridCount) {
            val y = paddingTop + (chartHeight * i / gridCount)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw line with rounded connections and gradient path below it
        val path = Path()
        val fillPath = Path()

        dataPoints.forEachIndexed { index, dp ->
            val x = index * xStep
            val yNormalized = (dp.value / maxValue).toFloat()
            val y = paddingTop + chartHeight - (yNormalized * chartHeight)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, paddingTop + chartHeight)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == numPoints - 1) {
                fillPath.lineTo(x, paddingTop + chartHeight)
                fillPath.close()
            }

            // Draw a neat point bubble
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }

        // Draw the filled area gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                startY = paddingTop,
                endY = paddingTop + chartHeight
            )
        )

        // Draw the smooth main line
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // Horizontal Labels
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dataPoints.forEach { dp ->
            Text(
                text = dp.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(55.dp),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun BreakdownCard(
    title: String,
    dataPoints: List<ChartDataPoint>,
    isCurrency: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val maxValue = dataPoints.maxOfOrNull { it.value } ?: 1.0

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                dataPoints.forEach { item ->
                    val progress = (item.value / maxValue).toFloat()
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            Text(
                                text = if (isCurrency) "₹${"%,.0f".format(item.value)}" else "${item.value.toInt()} tx",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    }
                }
            }
        }
    }
}
