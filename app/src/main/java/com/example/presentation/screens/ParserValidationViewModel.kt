package com.example.presentation.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.NotificationData
import com.example.domain.model.NotificationSource
import com.example.domain.model.ParseResult
import com.example.domain.parser.DuplicateDetector
import com.example.domain.parser.ParserEngine
import com.example.domain.parser.validation.ParserValidationFramework
import com.example.domain.parser.validation.ValidationReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParserValidationViewModel(
    private val parserEngine: ParserEngine,
    private val duplicateDetector: DuplicateDetector
) : ViewModel() {

    private val validationFramework = ParserValidationFramework(parserEngine, duplicateDetector)

    // Manual Parsing Screen States
    var manualInputText by mutableStateOf("")
    var manualSelectedApp by mutableStateOf(NotificationSource.GOOGLE_PAY)
    var manualParseResultState by mutableStateOf<ParseResult?>(null)
    var manualDuplicateDecision by mutableStateOf<String>("Not Evaluated")

    // Regression Suite States
    private val _isSuiteRunning = MutableStateFlow(false)
    val isSuiteRunning: StateFlow<Boolean> = _isSuiteRunning.asStateFlow()

    private val _validationReport = MutableStateFlow<ValidationReport?>(null)
    val validationReport: StateFlow<ValidationReport?> = _validationReport.asStateFlow()

    init {
        // Automatically execute the validation suite upon loading so the developer is greeted with live stats
        runFullSuite()
    }

    /**
     * Executes the manual parsing check for custom user pasted notifications.
     */
    fun runManualParse() {
        if (manualInputText.isBlank()) {
            manualParseResultState = ParseResult.Invalid("No input text provided")
            manualDuplicateDecision = "Skipped"
            return
        }

        val notif = NotificationData(
            packageName = manualSelectedApp.packageName,
            title = manualSelectedApp.displayName,
            text = manualInputText,
            bigText = null,
            postTime = System.currentTimeMillis(),
            notificationId = manualInputText.hashCode(),
            notificationKey = "manual_" + System.currentTimeMillis(),
            tag = null,
            extras = emptyMap()
        )

        // Evaluate Duplicate Decision before registering so we can show the diagnostic decision
        val isDupRaw = duplicateDetector.isDuplicate(notif)
        
        // Parse transaction
        val result = parserEngine.parseNotification(notif)
        manualParseResultState = result

        var dupState = "Clean"
        if (isDupRaw) {
            dupState = "Duplicate Raw (Blocked)"
        } else if (result is ParseResult.Success) {
            val isDupParsed = duplicateDetector.isDuplicateParsed(result.transaction)
            if (isDupParsed) {
                dupState = "Duplicate Parsed Signature (Blocked)"
            }
        }
        manualDuplicateDecision = dupState
    }

    /**
     * Executes the complete background automated regression suite and performance benchmark.
     */
    fun runFullSuite() {
        if (_isSuiteRunning.value) return

        viewModelScope.launch {
            _isSuiteRunning.value = true
            val report = withContext(Dispatchers.Default) {
                validationFramework.runValidationSuite()
            }
            _validationReport.value = report
            _isSuiteRunning.value = false
        }
    }

    fun clearDuplicateCache() {
        duplicateDetector.clearCache()
        manualDuplicateDecision = "Cache Cleared"
    }
}
