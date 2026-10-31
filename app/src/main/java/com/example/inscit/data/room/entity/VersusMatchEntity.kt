package com.example.inscit.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "versus_matches",
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
data class VersusMatchEntity(
    @PrimaryKey val matchId: String,
    val userId: String = "default_user",
    val opponentName: String = "",
    val isHost: Boolean = false,
    val hostScore: Int = 0,
    val guestScore: Int = 0,
    val result: String = "pending",
    val timestamp: Long = System.currentTimeMillis(),
    val questionsHash: String = ""
)
