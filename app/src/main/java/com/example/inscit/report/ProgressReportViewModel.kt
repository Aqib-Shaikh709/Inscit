package com.example.inscit.report

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProgressReportViewModel : ViewModel() {

    private val _generatedReport = MutableStateFlow<String?>(null)
    val generatedReport: StateFlow<String?> = _generatedReport.asStateFlow()

    fun generateReport(userDoc: com.example.inscit.models.UserDocument) {
        _generatedReport.value = DailyReportGenerator.generateTextReport(userDoc)
    }

    fun clearReport() {
        _generatedReport.value = null
    }
}

