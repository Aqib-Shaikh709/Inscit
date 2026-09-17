package com.example.inscit.xp

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.inscit.notifications.NotificationHelper
import java.util.concurrent.TimeUnit

object StreakTracker {
    private const val PREFS_NAME = "streak_tracker_prefs"
    private const val KEY_CURRENT_STREAK = "current_streak"
    private const val KEY_HIGHEST_STREAK = "highest_streak"
    private const val KEY_LAST_QUALIFYING_DATE = "last_qualifying_date"
    private const val KEY_LAST_NOTIFIED_DATE = "last_notified_date"
    private const val KEY_FREEZE_WEEK = "freeze_week_id"
    private const val WORK_NAME = "streak_daily_check"

    private fun getToday(): String = com.example.inscit.utils.DateUtils.today()

    private fun weekId(today: String): String {
        return try {
            val d = com.example.inscit.utils.DateUtils.parse(today) ?: return today
            val cal = java.util.Calendar.getInstance().apply { time = d }
            "${cal.get(java.util.Calendar.YEAR)}-W${cal.get(java.util.Calendar.WEEK_OF_YEAR)}"
        } catch (_: Exception) { today }
    }

    fun recordQuiz(context: Context, score: Float) {
        // Align with StreakManager: <60 ignored, 60-79 maintain, 80+ extend
        if (score < 60f) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = getToday()
        val lastDate = prefs.getString(KEY_LAST_QUALIFYING_DATE, "") ?: ""

        if (lastDate == today) return

        if (score < 80f) {
            // Maintain without increment: mark activity so reset logic doesn't break streak
            prefs.edit().putString(KEY_LAST_QUALIFYING_DATE, today).apply()
            scheduleDailyCheck(context)
            return
        }

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

        val last = com.example.inscit.utils.DateUtils.parse(lastDate) ?: return
        val now = com.example.inscit.utils.DateUtils.parse(today) ?: return
        val diffDays = TimeUnit.MILLISECONDS.toDays(now.time - last.time)

        // Freeze 1/week: consume freeze for exactly one missed day instead of resetting
        if (diffDays == 2L) {
            val wid = weekId(today)
            if (prefs.getString(KEY_FREEZE_WEEK, "") != wid) {
                prefs.edit()
                    .putString(KEY_FREEZE_WEEK, wid)
                    .putString(KEY_LAST_QUALIFYING_DATE, today)
                    .putString(KEY_LAST_NOTIFIED_DATE, today)
                    .apply()
                return
            }
        }

        if (diffDays >= 2) {
            val currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
            prefs.edit().putInt(KEY_CURRENT_STREAK, 0).apply()
            // Sync UserStats (single source of truth) - also reset inscit_prefs streak
            try {
                val inscitPrefs = context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
                val data = inscitPrefs.getString("user_data", null)
                if (data != null) {
                    val doc = com.example.inscit.UserDocumentSaver.restore(data)
                    if (doc != null && doc.stats.currentStreak != 0) {
                        val yesterday = com.example.inscit.utils.DateUtils.yesterday()
                        if (doc.stats.lastActivityDate != today && doc.stats.lastActivityDate != yesterday) {
                            val updated = doc.copy(stats = doc.stats.copy(currentStreak = 0))
                            val newData = com.example.inscit.serializeUserDocument(updated)
                            inscitPrefs.edit().putString("user_data", newData).apply()
                            try { context.openFileOutput("inscit_backup.dat", android.content.Context.MODE_PRIVATE).use { it.write(newData.toByteArray()) } } catch (_: Exception) {}
                        }
                    }
                }
            } catch (_: Exception) {}

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

    fun getCurrentStreak(context: Context): Int {
        val tracker = try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_CURRENT_STREAK, 0)
        } catch (_: Exception) { 0 }
        // Single-source fallback: if tracker empty but UserStats has streak, use it (removes duplicate drift)
        if (tracker != 0) return tracker
        return try {
            val data = context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
                .getString("user_data_json", null)
                ?: context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
                    .getString("user_data", null)
                ?: return 0
            com.example.inscit.parseUserDocumentJson(data)?.stats?.currentStreak
                ?: com.example.inscit.UserDocumentSaver.restore(data)?.stats?.currentStreak
                ?: 0
        } catch (_: Exception) { 0 }
    }

    fun getHighestStreak(context: Context): Int {
        val tracker = try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_HIGHEST_STREAK, 0)
        } catch (_: Exception) { 0 }
        if (tracker != 0) return tracker
        return try {
            val data = context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
                .getString("user_data_json", null)
                ?: context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
                    .getString("user_data", null)
                ?: return 0
            com.example.inscit.parseUserDocumentJson(data)?.stats?.longestStreak
                ?: com.example.inscit.UserDocumentSaver.restore(data)?.stats?.longestStreak
                ?: 0
        } catch (_: Exception) { 0 }
    }

    private fun scheduleDailyCheck(context: Context) {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        val workRequest = PeriodicWorkRequestBuilder<StreakCheckWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.DAYS)
            .setConstraints(constraints)
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
