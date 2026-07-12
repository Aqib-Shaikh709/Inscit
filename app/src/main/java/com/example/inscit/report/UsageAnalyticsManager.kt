package com.example.inscit.report

import com.example.inscit.models.UserDocument

object UsageAnalyticsManager {

    fun collectMetrics(userDoc: UserDocument): Map<String, String> {
        val stats = userDoc.stats
        val progress = userDoc.quizProgress
        
        val usageHours = stats.totalUsageTime / 3600000
        val usageMinutes = (stats.totalUsageTime % 3600000) / 60000
        
        val streakDisplay = if (stats.currentStreak > 0) {
            "🔥 ${stats.currentStreak} days"
        } else {
            "Not started"
        }
        
        val bestStreakDisplay = "${stats.longestStreak} days"
        
        return mapOf(
            "Usage Time" to "${usageHours}h ${usageMinutes}m",
            "Quiz Attempts" to stats.quizzesTaken.toString(),
            "Quiz Completions" to stats.quizzesTaken.toString(),
            "Average Score" to "${progress.lastScore.toInt()}%",
            "Lessons Opened" to userDoc.userNotes.size.toString(),
            "Current Streak" to streakDisplay,
            "Best Streak" to bestStreakDisplay
        )
    }
}
