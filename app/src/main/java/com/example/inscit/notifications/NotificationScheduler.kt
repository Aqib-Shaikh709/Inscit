package com.example.inscit.notifications

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val WORK_NAME = "inactivity_notification_work"

    // Periodic 24h with 4h flex + linear backoff. UPDATE (not REPLACE) so repeated
    // calls from onCreate/quiz-finish don't reset the timer or double-schedule.
    // InactivityWorker itself enforces the 1/day cap + toggle gate.
    fun scheduleInactivityNotification(context: Context) {
        if (!NotificationHelper.isEnabled(context)) {
            cancelAll(context)
            return
        }
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        val workRequest = PeriodicWorkRequestBuilder<InactivityWorker>(24, TimeUnit.HOURS, 4, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.MINUTES)
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
