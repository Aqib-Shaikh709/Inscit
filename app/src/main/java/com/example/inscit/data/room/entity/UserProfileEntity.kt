package com.example.inscit.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val userId: String = "default_user",
    val name: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val photoUrl: String? = null,
    val theme: String = "neon",
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val lastLoginAt: Long = 0
)
