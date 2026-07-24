package com.example.inscit.xp

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.inscit.notifications.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object StreakTracker {
    private const val PREFS_NAME = "streak_tracker_prefs"
    private const val KEY_CURRENT_STREAK = "current_streak"
    private const val KEY_HIGHEST_STREAK = "highest_streak"
    private const val KEY_LAST_QUALIFYING_DATE = "last_qualifying_date"
    private const val KEY_LAST_NOTIFIED_DATE = "last_notified_date"
    private const val WORK_NAME = "streak_daily_check"

    private fun getToday(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun recordQuiz(context: Context, score: Float) {
        if (score <= 70f) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = getToday()
        val lastDate = prefs.getString(KEY_LAST_QUALIFYING_DATE, "") ?: ""

        if (lastDate == today) return

        val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        val newStreak = currentStreak + 1
        val highestStreak = prefs.getInt(KEY_HIGHEST_STREAK, 0)
        val newHighest = maxOf(newStreak, highestStreak)

        prefs.edit()
            .putInt(KEY_CURRENT_STREAK, newStreak)
            .putInt(KEY_HIGHEST_STREAK, newHighest)
            .putString(KEY_LAST_QUALIFYING_DATE, today)
            .apply()

        scheduleDailyCheck(context)
    }

    fun checkAndResetIfMissed(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastDate = prefs.getString(KEY_LAST_QUALIFYING_DATE, "") ?: ""
        val today = getToday()
        val lastNotified = prefs.getString(KEY_LAST_NOTIFIED_DATE, "") ?: ""

        if (lastDate.isEmpty() || lastDate == today) return

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val last = sdf.parse(lastDate) ?: return
        val now = sdf.parse(today) ?: return
        val diffDays = TimeUnit.MILLISECONDS.toDays(now.time - last.time)

        if (diffDays >= 1) {
            val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
            prefs.edit().putInt(KEY_CURRENT_STREAK, 0).apply()

            if (lastNotified != today) {
                NotificationHelper.showNotification(
                    context,
                    title = "Streak Broken! 💔",
                    message = if (currentStreak > 0)
                        "Your $currentStreak-day streak has ended. Take a quiz to start a new one!"
                    else
                        "You haven't taken a quiz today. Keep learning!"
                )
                prefs.edit().putString(KEY_LAST_NOTIFIED_DATE, today).apply()
            }
        }
    }

    fun getCurrentStreak(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_CURRENT_STREAK, 0)

    fun getHighestStreak(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_HIGHEST_STREAK, 0)

    private fun scheduleDailyCheck(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<StreakCheckWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.DAYS)
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
