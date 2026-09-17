package com.example.inscit.xp

import com.example.inscit.models.UserStats

object StreakManager {
    
    private fun getCurrentDate(): String = com.example.inscit.utils.DateUtils.today()
    
    private fun getYesterdayDate(): String = com.example.inscit.utils.DateUtils.yesterday()
    
    // Single source thresholds: >=80 extend+bonus, >=60 maintain, <60 break.
    // Freeze 1/week is handled in StreakTracker.checkAndResetIfMissed (needs Context).
    fun updateStreak(stats: UserStats, scoreObtained: Float): UserStats {
        val currentDate = getCurrentDate()
        val yesterday = getYesterdayDate()

        return when {
            scoreObtained >= 80f -> {
                when {
                    stats.lastActivityDate == currentDate -> stats
                    stats.lastActivityDate == yesterday -> {
                        val newStreak = stats.currentStreak + 1
                        val newLongest = maxOf(newStreak, stats.longestStreak)
                        stats.copy(
                            currentStreak = newStreak,
                            longestStreak = newLongest,
                            lastActivityDate = currentDate
                        )
                    }
                    else -> {
                        stats.copy(
                            currentStreak = 1,
                            longestStreak = maxOf(1, stats.longestStreak),
                            lastActivityDate = currentDate
                        )
                    }
                }
            }
            scoreObtained >= 60f -> {
                // Maintain: keep streak count, just mark activity today so it doesn't break
                if (stats.lastActivityDate == currentDate) stats
                else stats.copy(lastActivityDate = currentDate)
            }
            else -> {
                if (stats.lastActivityDate == currentDate) {
                    stats
                } else if (stats.lastActivityDate == yesterday) {
                    stats.copy(
                        currentStreak = 0,
                        lastActivityDate = currentDate
                    )
                } else {
                    stats
                }
            }
        }
    }
}
