package com.example.presentation.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.error.ResultWrapper
import com.example.domain.repository.BackupRestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BackupUiState(
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val success: Boolean = false,
    val error: String? = null
)

data class RestoreUiState(
    val isProcessing: Boolean = false,
    val progress: Float = 0f,
    val success: Boolean = false,
    val error: String? = null
)

class BackupRestoreViewModel(
    private val backupRestoreRepository: BackupRestoreRepository
) : ViewModel() {

    private val _backupState = MutableStateFlow(BackupUiState())
    val backupState: StateFlow<BackupUiState> = _backupState.asStateFlow()

    private val _restoreState = MutableStateFlow(RestoreUiState())
    val restoreState: StateFlow<RestoreUiState> = _restoreState.asStateFlow()

    fun performBackup(uri: Uri) {
        _backupState.update { it.copy(isProcessing = true, progress = 0f, success = false, error = null) }
        viewModelScope.launch {
            val result = backupRestoreRepository.createBackup(uri) { progress ->
                _backupState.update { it.copy(progress = progress) }
            }
            when (result) {
                is ResultWrapper.Success -> {
                    _backupState.update { it.copy(isProcessing = false, progress = 1.0f, success = true) }
                }
                is ResultWrapper.Error -> {
                    _backupState.update { it.copy(isProcessing = false, error = result.error.message) }
                }
            }
        }
    }

    fun performRestore(uri: Uri) {
        _restoreState.update { it.copy(isProcessing = true, progress = 0f, success = false, error = null) }
        viewModelScope.launch {
            val result = backupRestoreRepository.restoreBackup(uri) { progress ->
                _restoreState.update { it.copy(progress = progress) }
            }
            when (result) {
                is ResultWrapper.Success -> {
                    _restoreState.update { it.copy(isProcessing = false, progress = 1.0f, success = true) }
                }
                is ResultWrapper.Error -> {
                    _restoreState.update { it.copy(isProcessing = false, error = result.error.message) }
                }
            }
        }
    }

    fun resetBackupState() {
        _backupState.update { BackupUiState() }
    }

    fun resetRestoreState() {
        _restoreState.update { RestoreUiState() }
    }
}
