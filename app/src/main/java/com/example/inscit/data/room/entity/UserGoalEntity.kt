package com.example.inscit.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_goals",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserGoalEntity(
    @PrimaryKey val id: String,
    val userId: String = "default_user",
    val title: String,
    val type: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val scoreThreshold: Int = 0,
    val dailyTarget: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0
)
