package com.example.inscit.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val WORK_NAME = "inactivity_notification_work"

    fun scheduleInactivityNotification(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<InactivityWorker>()
            .setInitialDelay(10, TimeUnit.HOURS) // Send after 10 hours of inactivity
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.KEEP, // Keep existing work, don't replace/reset timer
            workRequest
        )
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
