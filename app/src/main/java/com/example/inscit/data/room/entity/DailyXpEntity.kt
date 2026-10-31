package com.example.inscit.data.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_xp",
    foreignKeys = [
        ForeignKey(
            entity = UserProfileEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["userId", "date"]
)
data class DailyXpEntity(
    val userId: String = "default_user",
    val date: String,
    val xp: Int = 0
)
