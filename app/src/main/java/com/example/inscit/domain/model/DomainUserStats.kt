package com.example.inscit.domain.model

data class DomainUserStats(
    val xp: Int = 0,
    val level: Int = 1,
    val quizzesTaken: Int = 0,
    val completedChallengeDates: Set<String> = emptySet(),
    val totalUsageTime: Long = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: String = "",
    val lastScore: Float = 0f
)

data class DomainUserProfile(
    val userId: String = "default_user",
    val name: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val photoUrl: String? = null,
    val theme: String = "neon",
    val language: String = "en",
    val notificationsEnabled: Boolean = true,
    val lastLoginAt: Long = 0
)

data class DomainUserGoal(
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

enum class GoalType { XP, QUIZ }

data class DomainQuizAttempt(
    val attemptId: String,
    val score: Float,
    val xpEarned: Int,
    val timestamp: Long,
    val domainScores: Map<String, Float> = emptyMap(),
    val strengths: List<String> = emptyList(),
    val weaknesses: List<String> = emptyList()
)

data class DomainVersusMatch(
    val matchId: String,
    val opponentName: String,
    val isHost: Boolean,
    val hostScore: Int,
    val guestScore: Int,
    val result: String,
    val timestamp: Long,
    val questionsHash: String
)

data class DomainDailyXp(
    val date: String,
    val xp: Int
)

data class DomainRank(
    val label: String,
    val icon: String,
    val threshold: Int
)
