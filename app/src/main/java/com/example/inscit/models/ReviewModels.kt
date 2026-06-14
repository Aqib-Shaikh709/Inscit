package com.example.inscit.models

data class Review(
    val id: String,
    val userName: String,
    val rating: Int, // 1 to 5
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
