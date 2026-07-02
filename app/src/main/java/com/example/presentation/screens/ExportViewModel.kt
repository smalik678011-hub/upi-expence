package com.example.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.ResultWrapper
import com.example.domain.repository.ExportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.util.Date

enum class ExportFormat {
    CSV, PDF, EXCEL, JSON
}

enum class ExportStatus {
    IDLE, EXPORTING, SUCCESS, ERROR
}

data class ExportUiState(
    val format: ExportFormat = ExportFormat.CSV,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val status: ExportStatus = ExportStatus.IDLE,
    val progress: Float = 0f,
    val transactionCount: Int = 0,
    val errorMessage: String? = null,
    val lastExportedFileUriString: String? = null,
    val lastExportedFileName: String? = null
)

class ExportViewModel(
    private val exportRepository: ExportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    init {
        loadTransactionCount()
    }

    fun setFormat(format: ExportFormat) {
        _uiState.update { it.copy(format = format) }
    }

    fun setDateRange(startDate: Date?, endDate: Date?) {
        _uiState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate
            )
        }
        loadTransactionCount()
    }

    fun resetStatus() {
        _uiState.update {
            it.copy(
                status = ExportStatus.IDLE,
                progress = 0f,
                errorMessage = null
            )
        }
    }

    fun loadTransactionCount() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val result = exportRepository.getExpensesCount(state.startDate, state.endDate)
            if (result is ResultWrapper.Success) {
                _uiState.update { it.copy(transactionCount = result.data) }
            }
        }
    }

    fun performExport(
        outputStream: OutputStream,
        fileName: String,
        uriString: String,
        onComplete: (Boolean) -> Unit
    ) {
        _uiState.update {
            it.copy(
                status = ExportStatus.EXPORTING,
                progress = 0f,
                errorMessage = null
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val result = when (state.format) {
                ExportFormat.CSV -> {
                    exportRepository.exportToCsv(
                        outputStream = outputStream,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        onProgress = { progress ->
                            _uiState.update { it.copy(progress = progress) }
                        }
                    )
                }
                ExportFormat.PDF -> {
                    exportRepository.exportToPdf(
                        outputStream = outputStream,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        onProgress = { progress ->
                            _uiState.update { it.copy(progress = progress) }
                        }
                    )
                }
                else -> {
                    ResultWrapper.Error(
                        com.example.core.error.ErrorModel.DatabaseError.copy(message = "Format not supported yet")
                    )
                }
            }

            _uiState.update {
                when (result) {
                    is ResultWrapper.Success -> {
                        onComplete(true)
                        it.copy(
                            status = ExportStatus.SUCCESS,
                            progress = 1.0f,
                            lastExportedFileUriString = uriString,
                            lastExportedFileName = fileName
                        )
                    }
                    is ResultWrapper.Error -> {
                        onComplete(false)
                        it.copy(
                            status = ExportStatus.ERROR,
                            errorMessage = result.error.message ?: "Export failed"
                        )
                    }
                }
            }
        }
    }
}
