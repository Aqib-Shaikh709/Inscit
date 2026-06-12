package com.example.inscit.report

import com.example.inscit.models.UserDocument

object UsageAnalyticsManager {

    fun collectMetrics(userDoc: UserDocument): Map<String, String> {
        val stats = userDoc.stats
        val progress = userDoc.quizProgress
        
        val usageHours = stats.totalUsageTime / 3600000
        val usageMinutes = (stats.totalUsageTime % 3600000) / 60000
        
        return mapOf(
            "Usage Time" to "${usageHours}h ${usageMinutes}m",
            "Quiz Attempts" to stats.quizzesTaken.toString(),
            "Quiz Completions" to stats.quizzesTaken.toString(), // Simplified
            "Average Score" to "${progress.lastScore.toInt()}%",
            "Lessons Opened" to userDoc.userNotes.size.toString()
        )
    }
}
