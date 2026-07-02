package com.example.presentation.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.R
import com.example.core.utils.IndianFormattingUtils
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // 1. Storage Access Framework (SAF) Create Document Contract Launchers
    val csvSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            val contentResolver = context.contentResolver
            try {
                val outputStream = contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val dateSuffix = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    val fileName = "UPI_Report_$dateSuffix.csv"
                    viewModel.performExport(outputStream, fileName, uri.toString(), onComplete = { success ->
                        if (!success) {
                            Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val pdfSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            val contentResolver = context.contentResolver
            try {
                val outputStream = contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val dateSuffix = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    val fileName = "UPI_Report_$dateSuffix.pdf"
                    viewModel.performExport(outputStream, fileName, uri.toString(), onComplete = { success ->
                        if (!success) {
                            Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to trigger modern Storage Access Framework
    val onTriggerSaveToDevice = {
        val dateSuffix = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val defaultFileName = "UPI_Report_$dateSuffix"
        when (uiState.format) {
            ExportFormat.CSV -> csvSaveLauncher.launch("$defaultFileName.csv")
            ExportFormat.PDF -> pdfSaveLauncher.launch("$defaultFileName.pdf")
            else -> {
                Toast.makeText(context, "This format is not supported for export yet.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to generate file locally in cache and share via Android Share Sheet
    val onTriggerShare = {
        val dateSuffix = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val extension = if (uiState.format == ExportFormat.CSV) "csv" else "pdf"
        val fileName = "UPI_Report_$dateSuffix.$extension"
        
        val cacheDir = File(context.cacheDir, "reports")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        
        val file = File(cacheDir, fileName)
        try {
            val outputStream = FileOutputStream(file)
            val mimeType = if (uiState.format == ExportFormat.CSV) "text/csv" else "application/pdf"
            val fileProviderAuthority = "${context.packageName}.fileprovider"
            
            viewModel.performExport(outputStream, fileName, file.absolutePath, onComplete = { success ->
                if (success) {
                    val contentUri = FileProvider.getUriForFile(context, fileProviderAuthority, file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = mimeType
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        putExtra(Intent.EXTRA_SUBJECT, "UPI Transaction Report")
                        putExtra(Intent.EXTRA_TEXT, "Offline UPI Transaction Report generated on $dateSuffix.")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share UPI Report"))
                } else {
                    Toast.makeText(context, "Failed to generate report for sharing", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Error preparing file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("export_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.export_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .testTag("export_back_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Enterprise Banner Card
            EnterpriseBanner()

            // Step 1: Format Selection
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.export_format_label),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FormatChoiceCard(
                        title = "CSV Table",
                        subtitle = "Excel-ready spreadsheet",
                        icon = Icons.Default.GridOn,
                        selected = uiState.format == ExportFormat.CSV,
                        onClick = { viewModel.setFormat(ExportFormat.CSV) },
                        modifier = Modifier.weight(1f),
                        testTag = "format_csv_card"
                    )
                    
                    FormatChoiceCard(
                        title = "PDF Report",
                        subtitle = "Professional formatted document",
                        icon = Icons.Default.Description,
                        selected = uiState.format == ExportFormat.PDF,
                        onClick = { viewModel.setFormat(ExportFormat.PDF) },
                        modifier = Modifier.weight(1f),
                        testTag = "format_pdf_card"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FormatChoiceCard(
                        title = "Excel (.xlsx)",
                        subtitle = "Coming soon",
                        icon = Icons.Default.TableChart,
                        selected = false,
                        enabled = false,
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        testTag = "format_excel_card"
                    )
                    FormatChoiceCard(
                        title = "JSON Ledger",
                        subtitle = "Coming soon",
                        icon = Icons.Default.Code,
                        selected = false,
                        enabled = false,
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        testTag = "format_json_card"
                    )
                }
            }

            // Step 2: Date Range
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.export_range_label),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                // Date presets chips flow
                DatePresetsFlow(
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    onPresetSelected = { start, end ->
                        viewModel.setDateRange(start, end)
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Start/End Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DatePickerButton(
                        label = "Start Date",
                        date = uiState.startDate,
                        onClick = {
                            showDatePickerDialog(context, uiState.startDate) { selectedDate ->
                                // Ensure end date isn't before start date
                                val end = uiState.endDate
                                if (end != null && selectedDate.after(end)) {
                                    viewModel.setDateRange(selectedDate, selectedDate)
                                } else {
                                    viewModel.setDateRange(selectedDate, end)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "export_start_date_picker"
                    )
                    
                    DatePickerButton(
                        label = "End Date",
                        date = uiState.endDate,
                        onClick = {
                            showDatePickerDialog(context, uiState.endDate) { selectedDate ->
                                // Ensure start date isn't after end date
                                val start = uiState.startDate
                                if (start != null && selectedDate.before(start)) {
                                    viewModel.setDateRange(selectedDate, selectedDate)
                                } else {
                                    viewModel.setDateRange(start, selectedDate)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "export_end_date_picker"
                    )
                }
            }

            // Step 3: Preview Report Summary
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.export_preview_label),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                PreviewSummaryCard(
                    format = uiState.format,
                    startDate = uiState.startDate,
                    endDate = uiState.endDate,
                    transactionCount = uiState.transactionCount,
                    testTag = "export_preview_summary"
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTriggerSaveToDevice,
                    enabled = uiState.transactionCount > 0 && uiState.status != ExportStatus.EXPORTING,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_report_saf"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.export_btn_save),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                OutlinedButton(
                    onClick = onTriggerShare,
                    enabled = uiState.transactionCount > 0 && uiState.status != ExportStatus.EXPORTING,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_share_report_sheet"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.export_btn_share),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // Exporting overlay or progress dialog
    if (uiState.status == ExportStatus.EXPORTING) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { uiState.progress },
                        modifier = Modifier.size(56.dp)
                    )
                    
                    Text(
                        text = stringResource(R.string.export_progress_generating),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "${(uiState.progress * 100).toInt()}% complete",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Success Dialog
    if (uiState.status == ExportStatus.SUCCESS) {
        AlertDialog(
            onDismissRequest = { viewModel.resetStatus() },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF15803D),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.export_success_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.export_success_msg),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    if (uiState.lastExportedFileName != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (uiState.format == ExportFormat.CSV) Icons.Default.GridOn else Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = uiState.lastExportedFileName ?: "",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uriStr = uiState.lastExportedFileUriString
                        if (uriStr != null) {
                            openFile(context, uriStr, uiState.format)
                        }
                        viewModel.resetStatus()
                    },
                    modifier = Modifier.testTag("success_dialog_open_btn")
                ) {
                    Text(stringResource(R.string.export_btn_open))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.resetStatus() },
                    modifier = Modifier.testTag("success_dialog_close_btn")
                ) {
                    Text(stringResource(R.string.export_btn_close))
                }
            }
        )
    }

    // Error Dialog
    if (uiState.status == ExportStatus.ERROR) {
        AlertDialog(
            onDismissRequest = { viewModel.resetStatus() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.export_error_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = uiState.errorMessage ?: "An unknown error occurred during generation.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetStatus() },
                    modifier = Modifier.testTag("error_dialog_dismiss_btn")
                ) {
                    Text("Dismiss")
                }
            }
        )
    }
}

@Composable
fun EnterpriseBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Secure On-Device Reports",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Report generation runs fully offline. None of your transactional logs or financial history ever leaves your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FormatChoiceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val alpha = if (enabled) 1.0f else 0.4f
    
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            }
        ),
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = alpha))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = alpha),
                    modifier = Modifier.size(24.dp)
                )
                if (!enabled) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = "SOON",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (selected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DatePresetsFlow(
    startDate: Date?,
    endDate: Date?,
    onPresetSelected: (Date?, Date?) -> Unit
) {
    val presets = listOf(
        "All Time" to { onPresetSelected(null, null) },
        "This Month" to {
            val start = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time
            val end = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time
            onPresetSelected(start, end)
        },
        "Last Month" to {
            val start = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time
            val end = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time
            onPresetSelected(start, end)
        },
        "Last 30 Days" to {
            val start = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -30)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time
            val end = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time
            onPresetSelected(start, end)
        }
    )

    // Check currently matched preset
    val currentPresetName = when {
        startDate == null && endDate == null -> "All Time"
        isSameMonthAndCurrent(startDate, endDate) -> "This Month"
        isLastMonth(startDate, endDate) -> "Last Month"
        isLast30Days(startDate, endDate) -> "Last 30 Days"
        else -> "Custom Range"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEach { (name, action) ->
            val isSelected = currentPresetName == name
            FilterChip(
                selected = isSelected,
                onClick = action,
                label = { Text(name) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("preset_${name.lowercase().replace(" ", "_")}")
            )
        }
    }
}

@Composable
fun DatePickerButton(
    label: String,
    date: Date?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val formattedDate = if (date != null) {
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
    } else {
        "All Time"
    }

    OutlinedCard(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PreviewSummaryCard(
    format: ExportFormat,
    startDate: Date?,
    endDate: Date?,
    transactionCount: Int,
    testTag: String = ""
) {
    val reportDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    val dateRangeStr = if (startDate != null && endDate != null) {
        "${reportDateFormat.format(startDate)} to ${reportDateFormat.format(endDate)}"
    } else {
        "All-Time Transaction History"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (format == ExportFormat.CSV) Icons.Default.GridOn else Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (format == ExportFormat.CSV) "CSV Table Output" else "PDF Document Output",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Surface(
                    color = if (transactionCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$transactionCount Items",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (transactionCount > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Selected Scope:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateRangeStr,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }

            if (transactionCount == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "No transactions found in this range. Select a different range to export.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Dialog helper
fun showDatePickerDialog(context: Context, initialDate: Date?, onDateSelected: (Date) -> Unit) {
    val calendar = Calendar.getInstance()
    if (initialDate != null) {
        calendar.time = initialDate
    }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
        val result = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, selectedDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.time
        onDateSelected(result)
    }, year, month, day).show()
}

// Date calculation helpers for presets
private fun isSameMonthAndCurrent(start: Date?, end: Date?): Boolean {
    if (start == null || end == null) return false
    val cStart = Calendar.getInstance().apply { time = start }
    val cEnd = Calendar.getInstance().apply { time = end }
    val cCurrent = Calendar.getInstance()
    
    return cStart.get(Calendar.YEAR) == cCurrent.get(Calendar.YEAR) &&
            cStart.get(Calendar.MONTH) == cCurrent.get(Calendar.MONTH) &&
            cStart.get(Calendar.DAY_OF_MONTH) == 1 &&
            cEnd.get(Calendar.YEAR) == cCurrent.get(Calendar.YEAR) &&
            cEnd.get(Calendar.MONTH) == cCurrent.get(Calendar.MONTH)
}

private fun isLastMonth(start: Date?, end: Date?): Boolean {
    if (start == null || end == null) return false
    val cStart = Calendar.getInstance().apply { time = start }
    val cEnd = Calendar.getInstance().apply { time = end }
    val cLast = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
    
    return cStart.get(Calendar.YEAR) == cLast.get(Calendar.YEAR) &&
            cStart.get(Calendar.MONTH) == cLast.get(Calendar.MONTH) &&
            cStart.get(Calendar.DAY_OF_MONTH) == 1 &&
            cEnd.get(Calendar.YEAR) == cLast.get(Calendar.YEAR) &&
            cEnd.get(Calendar.MONTH) == cLast.get(Calendar.MONTH) &&
            cEnd.get(Calendar.DAY_OF_MONTH) == cLast.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun isLast30Days(start: Date?, end: Date?): Boolean {
    if (start == null || end == null) return false
    val cStart = Calendar.getInstance().apply { time = start }
    val cEnd = Calendar.getInstance().apply { time = end }
    val cCurrent = Calendar.getInstance()
    
    val diff = cCurrent.timeInMillis - cStart.timeInMillis
    val daysDiff = diff / (24 * 60 * 60 * 1000)
    
    return daysDiff in 29..31 && cEnd.get(Calendar.YEAR) == cCurrent.get(Calendar.YEAR) && cEnd.get(Calendar.MONTH) == cCurrent.get(Calendar.MONTH)
}

private fun openFile(context: Context, uriString: String, format: ExportFormat) {
    try {
        val uri = if (uriString.startsWith("content://")) {
            Uri.parse(uriString)
        } else {
            // Local file path (shared from cache)
            val file = File(uriString)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
        
        val mimeType = if (format == ExportFormat.CSV) "text/csv" else "application/pdf"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open Report"))
    } catch (e: Exception) {
        Toast.makeText(context, "No app found to open this report file type. Please install a compatible CSV viewer or PDF reader.", Toast.LENGTH_LONG).show()
    }
}
