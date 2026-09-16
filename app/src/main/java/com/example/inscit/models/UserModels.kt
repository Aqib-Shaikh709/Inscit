package com.example.inscit.models

import kotlinx.serialization.Serializable

@Serializable
enum class Lang { EN, HI }
@Serializable
enum class ThemeMode { NEON, NOBLE, CUSTOM }

@Serializable
data class UserSettings(
    val language: Lang = Lang.EN,
    val theme: ThemeMode = ThemeMode.NEON,
    val lastReportDate: Long = 0
)

@Serializable
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val photoUrl: String? = null
)

@Serializable
data class UserStats(
    val xp: Int = 0,
    val level: Int = 1,
    val quizzesTaken: Int = 0,
    val completedChallengeDates: Set<String> = emptySet(), // Format: "yyyy-MM-dd"
    val totalUsageTime: Long = 0, // in milliseconds
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: String = "" // Format: "yyyy-MM-dd"
)

@Serializable
data class DailyChallengeStatus(
    val lastCompletionDate: String = "",
    val currentRound: Int = 1,
    val isCompletedToday: Boolean = false
)

@Serializable
data class QuizProgress(
    val lastScore: Float = 0f,
    val domainScores: Map<String, Float> = emptyMap(),
    val strengths: List<String> = emptyList(),
    val weaknesses: List<String> = emptyList()
)

@Serializable
data class UserNote(
    val content: String = "",
    val drawingData: String = "" // Simplified drawing representation
)

@Serializable
enum class GoalType { XP, QUIZ }

@Serializable
data class UserGoal(
    val id: String,
    val title: String,
    val type: GoalType,
    val targetValue: Int,
    val currentValue: Int = 0,
    val scoreThreshold: Int = 0,
    val dailyTarget: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0
)

@Serializable
data class UserDocument(
    val profile: UserProfile = UserProfile(),
    val stats: UserStats = UserStats(),
    val quizProgress: QuizProgress = QuizProgress(),
    val settings: UserSettings = UserSettings(),
    val userNotes: Map<String, UserNote> = emptyMap(),
    val dailyChallengeStatus: DailyChallengeStatus = DailyChallengeStatus(),
    val goals: List<UserGoal> = emptyList(),
    val dailyXp: Map<String, Int> = emptyMap() // key: "yyyy-MM-dd" -> XP earned that day
)

@Serializable
data class QuizAttempt(
    val attemptId: String = "",
    val score: Float = 0f,
    val xpEarned: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
