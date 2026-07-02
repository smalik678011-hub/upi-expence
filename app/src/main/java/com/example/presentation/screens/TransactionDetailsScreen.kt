package com.example.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.utils.IndianFormattingUtils
import com.example.domain.model.Expense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    id: String,
    viewModel: TransactionDetailsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Load transaction once
    LaunchedEffect(id) {
        viewModel.loadTransaction(id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transaction Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.testTag("details_title")
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_details_back")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is TransactionDetailsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.testTag("details_loading"))
                    }
                }
                is TransactionDetailsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.testTag("details_error_text")
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadTransaction(id) }) {
                            Text("Retry")
                        }
                    }
                }
                is TransactionDetailsUiState.Success -> {
                    val expense = state.expense
                    TransactionDetailsContent(
                        expense = expense,
                        viewModel = viewModel,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionDetailsContent(
    expense: Expense,
    viewModel: TransactionDetailsViewModel,
    context: Context
) {
    val scrollState = rememberScrollState()
    var isSmsExpanded by remember { mutableStateOf(false) }

    // Category editor variables
    var showCategoryDialog by remember { mutableStateOf(false) }
    
    // Status editor variables
    var showStatusDialog by remember { mutableStateOf(false) }

    val isDebit = expense.uType.equals("DEBIT", ignoreCase = true)
    val themeColor = if (isDebit) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("details_hero_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Direction indicator badge
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDebit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = themeColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount
                Text(
                    text = (if (isDebit) "-" else "+") + IndianFormattingUtils.formatIndianCurrency(expense.amount),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor,
                    modifier = Modifier.testTag("details_amount")
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Counterparty Name
                Text(
                    text = expense.merchantName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("details_merchant")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category & Status pill
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = { showCategoryDialog = true },
                        label = { Text("Category: ${expense.category}") },
                        icon = { Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("chip_details_category")
                    )

                    SuggestionChip(
                        onClick = { showStatusDialog = true },
                        label = { Text(expense.status) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            labelColor = when (expense.status) {
                                "SUCCESS" -> Color(0xFF2E7D32)
                                "PENDING" -> Color(0xFFE65100)
                                "FAILED" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        ),
                        modifier = Modifier.testTag("chip_details_status")
                    )
                }
            }
        }

        // Details Block
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "TRANSACTION INFORMATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )

                DetailRow(label = "Date & Time", value = IndianFormattingUtils.formatIndianDate(expense.date))
                DetailRow(label = "Payment Direction", value = if (isDebit) "Sent (Debit)" else "Received (Credit)")
                DetailRow(label = "Source Application", value = expense.accountOrBank ?: "Not Available")
                DetailRow(label = "UPI Reference ID", value = expense.transactionRef ?: "Not Available")
                DetailRow(label = "Category", value = expense.category)
                DetailRow(label = "Database Entry ID", value = expense.id)
            }
        }

        // Raw SMS Body Collapsible Section
        if (!expense.rawSmsBody.isNullOrBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sms_collapsible_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSmsExpanded = !isSmsExpanded }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Inspection: Raw Notification SMS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = if (isSmsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isSmsExpanded) "Collapse" else "Expand"
                        )
                    }

                    AnimatedVisibility(visible = isSmsExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = expense.rawSmsBody,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.testTag("raw_sms_body_text")
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Raw SMS", expense.rawSmsBody)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied SMS to Clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("btn_copy_sms")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy SMS")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Share formatted receipt recipe
            Button(
                onClick = {
                    val recipeText = """
                        === UPI TRANSACTION RECIPE ===
                        Merchant: ${expense.merchantName}
                        Amount: ${if (isDebit) "-" else "+"}${IndianFormattingUtils.formatIndianCurrency(expense.amount)}
                        Date: ${IndianFormattingUtils.formatIndianDate(expense.date)}
                        Ref ID: ${expense.transactionRef ?: "N/A"}
                        App: ${expense.accountOrBank ?: "N/A"}
                        Status: ${expense.status}
                        Category: ${expense.category}
                        ==============================
                    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, recipeText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Transaction Recipe"))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_share_recipe"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Recipe", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Category Selector Dialog
    if (showCategoryDialog) {
        val categories = listOf("Uncategorized", "Food", "Groceries", "Shopping", "Fuel", "Travel", "Bills", "Salary", "Refund", "Entertainment", "Medical")
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            text = {
                Column {
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateCategory(expense, cat)
                                    showCategoryDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat,
                                fontSize = 16.sp,
                                fontWeight = if (expense.category == cat) FontWeight.Bold else FontWeight.Normal,
                                color = if (expense.category == cat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Close")
                }
            },
            modifier = Modifier.testTag("category_selector_dialog")
        )
    }

    // Status Override Dialog (Sandbox)
    if (showStatusDialog) {
        val statuses = listOf("SUCCESS", "PENDING", "FAILED")
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Override Status (Sandbox)") },
            text = {
                Column {
                    statuses.forEach { stat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateStatus(expense, stat)
                                    showStatusDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stat,
                                fontSize = 16.sp,
                                fontWeight = if (expense.status == stat) FontWeight.Bold else FontWeight.Normal,
                                color = if (expense.status == stat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Close")
                }
            },
            modifier = Modifier.testTag("status_selector_dialog")
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("detail_val_${label.lowercase().replace(" ", "_")}")
        )
    }
}
