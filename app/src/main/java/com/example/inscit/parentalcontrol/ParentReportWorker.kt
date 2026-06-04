package com.example.inscit.parentalcontrol

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.inscit.loadUserDocument
import com.example.inscit.saveUserDocument

class ParentReportWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val context = applicationContext
        val userDoc = loadUserDocument(context)
        
        val parentEmail = userDoc.settings.parentEmail
        if (parentEmail.isEmpty()) {
            return Result.success()
        }

        // Check if we already sent a report today
        val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
        val lastReportDay = userDoc.settings.lastReportDate / (24 * 60 * 60 * 1000)
        
        if (today == lastReportDay) {
            return Result.success()
        }

        // Generate Report
        val htmlContent = DailyReportGenerator.generateHtmlReport(userDoc)
        
        // Isolate sending logic
        sendEmail(parentEmail, "Daily Learning Report Card", htmlContent)

        // Update last report date
        val updatedDoc = userDoc.copy(
            settings = userDoc.settings.copy(lastReportDate = System.currentTimeMillis())
        )
        saveUserDocument(context, updatedDoc)

        return Result.success()
    }

    private fun sendEmail(to: String, subject: String, body: String) {
        // Since no SMTP client is provided, we prepare the payload and log it.
        // In a real production app with a backend, this would be an API call.
        // With an SMTP library, it would be a MimeMessage send.
        Log.d("ParentReportWorker", "Sending Email to: $to")
        Log.d("ParentReportWorker", "Subject: $subject")
        Log.d("ParentReportWorker", "Body: $body")
    }
}
