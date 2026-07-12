package com.example.inscit.xp

import com.example.inscit.models.UserStats
import java.text.SimpleDateFormat
import java.util.*

object StreakManager {
    
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(Date())
    }
    
    private fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return dateFormat.format(calendar.time)
    }
    
    fun updateStreak(stats: UserStats, scoreObtained: Float): UserStats {
        val currentDate = getCurrentDate()
        val yesterday = getYesterdayDate()
        
        return if (scoreObtained >= 80f) {
            // Score is 80 or above
            when {
                stats.lastActivityDate == currentDate -> {
                    // Already scored 80+ today, no change
                    stats
                }
                stats.lastActivityDate == yesterday -> {
                    // Streak continues - scored yesterday and today
                    val newStreak = stats.currentStreak + 1
                    val newLongest = maxOf(newStreak, stats.longestStreak)
                    stats.copy(
                        currentStreak = newStreak,
                        longestStreak = newLongest,
                        lastActivityDate = currentDate
                    )
                }
                else -> {
                    // New streak or gap - start fresh
                    stats.copy(
                        currentStreak = 1,
                        lastActivityDate = currentDate
                    )
                }
            }
        } else {
            // Score is below 80
            if (stats.lastActivityDate == currentDate) {
                // Already reset today or haven't attempted yet today
                stats
            } else if (stats.lastActivityDate == yesterday) {
                // Miss a day - streak breaks
                stats.copy(
                    currentStreak = 0,
                    lastActivityDate = currentDate
                )
            } else {
                // Already broken or no activity
                stats
            }
        }
    }
}
