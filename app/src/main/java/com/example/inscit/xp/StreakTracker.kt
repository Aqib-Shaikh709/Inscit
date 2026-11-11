package com.example.inscit.xp

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.inscit.notifications.NotificationHelper
import java.util.concurrent.TimeUnit

/**
 * Streak tracking with a single source of truth: [com.example.inscit.models.UserStats]
 * in `inscit_prefs` (JSON). The old `streak_tracker_prefs` is retired — it diverged
 * from `UserStats.currentStreak` and caused conflicting streak values across screens
 * and widgets.
 */
object StreakTracker {

    private const val PREFS_NAME = "inscit_prefs"
    private const val KEY_JSON = "user_data_json"
    private const val KEY_LEGACY = "user_data"
    private const val KEY_FREEZE_WEEK = "streak_freeze_week"
    private const val KEY_LAST_NOTIFIED_DATE = "streak_last_notified_date"
    private const val WORK_NAME = "streak_daily_check"

    private fun getToday(): String = com.example.inscit.utils.DateUtils.today()

    private fun weekId(today: String): String {
        return try {
            val d = com.example.inscit.utils.DateUtils.parse(today) ?: return today
            val cal = java.util.Calendar.getInstance().apply { time = d }
            "${cal.get(java.util.Calendar.YEAR)}-W${cal.get(java.util.Calendar.WEEK_OF_YEAR)}"
        } catch (_: Exception) { today }
    }

    /** Read the current UserDocument from inscit_prefs (JSON primary, legacy fallback). */
    private fun readDoc(context: Context): com.example.inscit.models.UserDocument? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val data = prefs.getString(KEY_JSON, null)
            ?: prefs.getString(KEY_LEGACY, null)
            ?: return null
        return com.example.inscit.parseUserDocumentJson(data)
            ?: com.example.inscit.UserDocumentSaver.restore(data)
    }

    /** Persist a UserDocument to inscit_prefs (JSON). */
    private fun writeDoc(context: Context, doc: com.example.inscit.models.UserDocument) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = com.example.inscit.serializeUserDocumentJson(doc)
        prefs.edit().putString(KEY_JSON, json).apply()
    }

    fun recordQuiz(context: Context, score: Float) {
        // Align with StreakManager: <60 ignored, 60-79 maintain, 80+ extend
        if (score < 60f) return

        val today = getToday()
        val doc = readDoc(context) ?: return

        // Already recorded today
        if (doc.stats.lastActivityDate == today) return

        // Update streak via StreakManager (canonical logic)
        val newStats = StreakManager.updateStreak(doc.stats, score)
        val updatedDoc = doc.copy(stats = newStats)

        writeDoc(context, updatedDoc)
        scheduleDailyCheck(context)
    }

    fun checkAndResetIfMissed(context: Context) {
        val today = getToday()
        val doc = readDoc(context) ?: return

        val lastActivityDate = doc.stats.lastActivityDate
        if (lastActivityDate.isEmpty() || lastActivityDate == today) return

        val last = com.example.inscit.utils.DateUtils.parse(lastActivityDate) ?: return
        val now = com.example.inscit.utils.DateUtils.parse(today) ?: return
        val diffDays = TimeUnit.MILLISECONDS.toDays(now.time - last.time)

        if (diffDays < 2) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastNotified = prefs.getString(KEY_LAST_NOTIFIED_DATE, "") ?: ""

        // Freeze 1/week: consume freeze for exactly one missed day instead of resetting
        if (diffDays == 2L) {
            val wid = weekId(today)
            if (prefs.getString(KEY_FREEZE_WEEK, "") != wid) {
                prefs.edit()
                    .putString(KEY_FREEZE_WEEK, wid)
                    .putString(KEY_LAST_NOTIFIED_DATE, today)
                    .apply()
                return
            }
        }

        if (diffDays >= 2) {
            val currentStreak = doc.stats.currentStreak
            val updated = doc.copy(stats = doc.stats.copy(currentStreak = 0))
            writeDoc(context, updated)

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
        return try {
            readDoc(context)?.stats?.currentStreak ?: 0
        } catch (_: Exception) { 0 }
    }

    fun getHighestStreak(context: Context): Int {
        return try {
            readDoc(context)?.stats?.longestStreak ?: 0
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
