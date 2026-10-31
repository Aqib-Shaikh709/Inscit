package com.example.inscit.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "quiz_attempts",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId", "timestamp"])]
)
data class QuizAttemptEntity(
    @PrimaryKey val attemptId: String,
    val userId: String = "default_user",
    val score: Float = 0f,
    val xpEarned: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val domainScores: String = "{}",
    val strengths: String = "[]",
    val weaknesses: String = "[]"
)
