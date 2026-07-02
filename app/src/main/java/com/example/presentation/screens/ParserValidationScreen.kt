package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.NotificationSource
import com.example.domain.model.ParseResult
import com.example.domain.model.TransactionDirection
import com.example.domain.model.TransactionStatus
import com.example.domain.parser.validation.ExpectedType
import com.example.domain.parser.validation.SingleValidationResult
import com.example.domain.parser.validation.ValidationReport

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ParserValidationScreen(
    viewModel: ParserValidationViewModel,
    onNavigateBack: () -> Unit
) {
    val isSuiteRunning by viewModel.isSuiteRunning.collectAsState()
    val validationReport by viewModel.validationReport.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Diagnostic Suite", "Manual Sandbox")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Parser Validator",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        modifier = Modifier.testTag("tab_$index")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> DiagnosticSuiteTab(
                    isSuiteRunning = isSuiteRunning,
                    report = validationReport,
                    onRunSuite = { viewModel.runFullSuite() },
                    onClearCache = { viewModel.clearDuplicateCache() }
                )
                1 -> ManualSandboxTab(
                    inputText = viewModel.manualInputText,
                    onInputTextChange = { viewModel.manualInputText = it },
                    selectedApp = viewModel.manualSelectedApp,
                    onAppSelected = { viewModel.manualSelectedApp = it },
                    parseResult = viewModel.manualParseResultState,
                    duplicateDecision = viewModel.manualDuplicateDecision,
                    onParse = { viewModel.runManualParse() },
                    onClearCache = { viewModel.clearDuplicateCache() },
                    onPaste = {
                        clipboardManager.getText()?.let {
                            viewModel.manualInputText = it.text
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DiagnosticSuiteTab(
    isSuiteRunning: Boolean,
    report: ValidationReport?,
    onRunSuite: () -> Unit,
    onClearCache: () -> Unit
) {
    if (isSuiteRunning) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("suite_loading_indicator"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Running 25+ Regression & Benchmark Tests...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else if (report != null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("suite_report_view"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Stats Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Regression Summary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryStatItem(
                                label = "Passed Tests",
                                value = "${report.passedTests}/${report.totalTests}",
                                color = if (report.failedTests == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            SummaryStatItem(
                                label = "Failed Tests",
                                value = "${report.failedTests}",
                                color = if (report.failedTests == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                            )
                            SummaryStatItem(
                                label = "Accuracy",
                                value = "%.1f%%".format(report.overallSuccessRate),
                                color = if (report.overallSuccessRate == 100.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryStatItem(
                                label = "Ignored (Promo)",
                                value = "${report.ignoredTests}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            SummaryStatItem(
                                label = "Invalid/Malformed",
                                value = "${report.invalidTests}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            SummaryStatItem(
                                label = "Total Time",
                                value = "${report.totalExecutionTimeMs} ms",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Benchmark Performance Card
            report.benchmarkReport?.let { bench ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Parser Benchmark Metrics",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SummaryStatItem(
                                    label = "Stress Loop Run",
                                    value = "${bench.totalProcessed} parses",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                SummaryStatItem(
                                    label = "Throughput",
                                    value = "%.0f parses/sec".format(bench.throughputPerSec),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SummaryStatItem(
                                    label = "Avg Latency",
                                    value = "%.2f ms".format(bench.avgLatencyMs),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                SummaryStatItem(
                                    label = "Approx Memory",
                                    value = "+${bench.memoryDeltaKb} KB",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Duplicate Detection Check Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (report.duplicateTestPassed) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (report.duplicateTestPassed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Duplicate Prevention Verification",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = report.duplicateTestDetails,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Test Actions Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onRunSuite,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_run_suite"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Re-run Diagnostics")
                    }

                    OutlinedButton(
                        onClick = onClearCache,
                        modifier = Modifier.testTag("btn_clear_dup_cache")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Cache")
                    }
                }
            }

            // Detailed Test Cases
            item {
                Text(
                    text = "Detailed Regression Log (${report.results.size} scenarios)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(report.results) { result ->
                SingleTestResultCard(result)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onRunSuite, modifier = Modifier.testTag("btn_start_suite")) {
                Text("Start Parser Validation Suite")
            }
        }
    }
}

@Composable
fun ManualSandboxTab(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    selectedApp: NotificationSource,
    onAppSelected: (NotificationSource) -> Unit,
    parseResult: ParseResult?,
    duplicateDecision: String,
    onParse: () -> Unit,
    onClearCache: () -> Unit,
    onPaste: () -> Unit
) {
    val supportedApps = listOf(
        NotificationSource.GOOGLE_PAY,
        NotificationSource.PHONEPE,
        NotificationSource.PAYTM,
        NotificationSource.NAVI
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("sandbox_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Picker Row
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Simulated App Source Package",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                supportedApps.forEach { app ->
                    val isSelected = selectedApp == app
                    FilterChip(
                        selected = isSelected,
                        onClick = { onAppSelected(app) },
                        label = { Text(app.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("chip_${app.name.lowercase()}")
                    )
                }
            }
        }

        // Notification Input TextField
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notification Message Text",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )

                IconButton(
                    onClick = onPaste,
                    modifier = Modifier.size(24.dp).testTag("btn_paste_clipboard")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste from clipboard",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .testTag("sandbox_input_field"),
                placeholder = {
                    Text(
                        text = "Paste standard UPI notification text here (e.g., 'Paid Rs 150 to Ramesh successful.')",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onParse,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_parse_sandbox"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Analyze Notification")
            }

            OutlinedButton(
                onClick = onClearCache,
                modifier = Modifier.testTag("btn_clear_dup_cache_sandbox")
            ) {
                Icon(imageVector = Icons.Default.ClearAll, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear Cache")
            }
        }

        // Duplicate decision badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (duplicateDecision) {
                    "Clean" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    "Cache Cleared" -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = when (duplicateDecision) {
                        "Clean" -> MaterialTheme.colorScheme.primary
                        "Cache Cleared" -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Duplicate Check Engine: $duplicateDecision",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = when (duplicateDecision) {
                        "Clean" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "Cache Cleared" -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
        }

        // Parser results output
        if (parseResult != null) {
            Text(
                text = "Diagnostic Report",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            when (parseResult) {
                is ParseResult.Success -> {
                    val txn = parseResult.transaction
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .testTag("parsed_txn_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header: Direction Badge & Amount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DirectionBadge(txn.direction)
                                Text(
                                    text = "₹${"%,.2f".format(txn.amount)}",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Counterparty & App row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Counterparty",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = txn.counterpartyName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "App Source",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = txn.sourceApp.displayName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            // Ref ID & Status row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "UTR / Ref No",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = txn.transactionRef ?: "NOT FOUND",
                                        fontFamily = FontFamily.Monospace,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Parsed Status",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    StatusBadge(txn.status)
                                }
                            }

                            // Confidence Score Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Confidence Level",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val pct = (txn.confidence * 100).toInt()
                                    Text(
                                        text = "$pct%",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (pct >= 80) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
                is ParseResult.Ignored -> {
                    DiagnosticFailureCard(
                        title = "Ignored Notification",
                        description = "This message was classified as non-transactional (promotional, security, or info alert) and ignored successfully.",
                        reason = parseResult.reason,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                is ParseResult.Invalid -> {
                    DiagnosticFailureCard(
                        title = "Invalid Parse Structure",
                        description = "The message did not match the minimal syntax criteria needed to parse a valid financial log (such as missing a valid payment amount or counterpart).",
                        reason = parseResult.reason,
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    )
                }
                is ParseResult.Failed -> {
                    DiagnosticFailureCard(
                        title = "Parsing Exception",
                        description = "An unexpected error occurred in the parsing codebase while attempting to process this raw notification text.",
                        reason = parseResult.reason,
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SingleTestResultCard(result: SingleValidationResult) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (result.passed) Color.Transparent else MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (result.passed) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (result.passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.scenario,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = result.appSource.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Expected: ${result.expectedType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "%.2f ms".format(result.latencyMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Result Summary: ${result.actualResult}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    result.errorDetails?.let { err ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Diagnostic Error: $err",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticFailureCard(
    title: String,
    description: String,
    reason: String,
    containerColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Reason code: $reason",
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun SummaryStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
fun DirectionBadge(direction: TransactionDirection) {
    val (label, containerColor, textColor) = when (direction) {
        TransactionDirection.SENT -> Triple("Debit / Outgoing", Color(0xFFFEEBEE), Color(0xFFC62828))
        TransactionDirection.RECEIVED -> Triple("Credit / Incoming", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        else -> Triple("Unknown Direction", Color(0xFFECEFF1), Color(0xFF37474F))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor
        )
    }
}

@Composable
fun StatusBadge(status: TransactionStatus) {
    val (label, containerColor, textColor) = when (status) {
        TransactionStatus.SUCCESS -> Triple("SUCCESS", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        TransactionStatus.FAILED -> Triple("FAILED", Color(0xFFFEEBEE), Color(0xFFC62828))
        TransactionStatus.PENDING -> Triple("PENDING", Color(0xFFFFF8E1), Color(0xFFF57F17))
        else -> Triple("UNKNOWN", Color(0xFFECEFF1), Color(0xFF37474F))
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = textColor
        )
    }
}
