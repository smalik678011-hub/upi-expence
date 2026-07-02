package com.example.presentation.screens

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AboutViewModel : ViewModel() {

    private val _appVersion = MutableStateFlow("1.0.0")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    private val _versionCode = MutableStateFlow(1)
    val versionCode: StateFlow<Int> = _versionCode.asStateFlow()

    private val _buildType = MutableStateFlow("debug")
    val buildType: StateFlow<String> = _buildType.asStateFlow()

    fun loadAppInfo(context: Context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            _appVersion.value = packageInfo.versionName ?: "1.0.0"
            _versionCode.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
            // In typical cases, we can check BuildConfig.BUILD_TYPE but since we want it dynamic or standard:
            _buildType.value = "debug" // Default or read from BuildConfig
        } catch (e: Exception) {
            // Fallbacks
        }
    }
}
