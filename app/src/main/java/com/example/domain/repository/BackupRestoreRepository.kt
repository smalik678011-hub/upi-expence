package com.example.domain.repository

import android.net.Uri
import com.example.core.error.ResultWrapper

interface BackupRestoreRepository {
    suspend fun createBackup(uri: Uri, onProgress: (Float) -> Unit): ResultWrapper<Unit>
    suspend fun restoreBackup(uri: Uri, onProgress: (Float) -> Unit): ResultWrapper<Unit>
}
