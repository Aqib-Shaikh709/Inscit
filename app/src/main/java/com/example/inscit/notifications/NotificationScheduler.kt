package com.example.inscit.notifications

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val WORK_NAME = "inactivity_notification_work"
    private const val INITIAL_WORK_NAME = "inactivity_notification_initial"

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
        // First run would otherwise wait a full period: one nudge a few hours after
        // install, then the periodic cadence takes over. KEEP policy = scheduled once
        // ever; the worker's own 1/day cap prevents any double ping with the periodic run.
        val initial = OneTimeWorkRequestBuilder<InactivityWorker>()
            .setInitialDelay(3, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.MINUTES)
            .addTag(INITIAL_WORK_NAME)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            INITIAL_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            initial
        )
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
