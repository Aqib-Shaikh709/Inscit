package com.example.inscit.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_stats",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserStatsEntity(
    @PrimaryKey val userId: String = "default_user",
    val xp: Int = 0,
    val level: Int = 1,
    val quizzesTaken: Int = 0,
    val completedChallengeDates: String = "",
    val totalUsageTime: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: String = "",
    val lastScore: Float = 0f,
    val dailyXp: String = "{}"
)
