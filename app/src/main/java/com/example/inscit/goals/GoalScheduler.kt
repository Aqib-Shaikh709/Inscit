package com.example.inscit.goals

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.inscit.models.GoalType
import com.example.inscit.notifications.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object GoalScheduler {
    private const val WORK_NAME = "goal_daily_reminder"

    fun scheduleDailyGoalReminder(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<GoalReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.DAYS)
            .addTag(WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}

class GoalReminderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            val context = applicationContext
            val goals = GoalManager.loadGoalsFromPrefs(context)
            val active = goals.filter { !it.isCompleted }
            if (active.isEmpty()) {
                return Result.success()
            }

            val prefs = context.getSharedPreferences("goal_reminder_prefs", Context.MODE_PRIVATE)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            if (prefs.getString("last_notified_date", "") == today) {
                return Result.success()
            }

            val xpGoal = active.firstOrNull { it.type == GoalType.XP }
            val quizGoal = active.firstOrNull { it.type == GoalType.QUIZ }

            val title = "🎯 YOUR GOALS AWAIT"
            val message = when {
                xpGoal != null -> {
                    val remaining = (xpGoal.targetValue - xpGoal.currentValue).coerceAtLeast(0)
                    "You're $remaining XP away from '${xpGoal.title}'. Take a quiz to keep pushing!"
                }
                quizGoal != null -> {
                    val remaining = (quizGoal.targetValue - quizGoal.currentValue).coerceAtLeast(0)
                    "Only $remaining more ${if (remaining == 1) "quiz" else "quizzes"} with ${quizGoal.scoreThreshold}%+ score for '${quizGoal.title}'!"
                }
                else -> "Open the Goal Maker and set a new personal goal today!"
            }

            NotificationHelper.showNotification(context, title, message)
            prefs.edit().putString("last_notified_date", today).apply()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}