package com.example.inscit.data.repository

import com.example.inscit.domain.model.DomainQuizAttempt
import com.example.inscit.domain.model.DomainUserProfile
import com.example.inscit.domain.model.DomainUserStats
import com.example.inscit.domain.model.DomainUserGoal
import com.example.inscit.domain.model.DomainVersusMatch
import com.example.inscit.domain.model.GoalType
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // Profile
    suspend fun getProfile(): DomainUserProfile?
    suspend fun saveProfile(profile: DomainUserProfile)
    suspend fun updateLoginTime()

    // Stats
    suspend fun getStats(): DomainUserStats
    suspend fun updateStats(stats: DomainUserStats)
    suspend fun incrementXp(xp: Int)
    suspend fun updateStreak(score: Float)

    // Goals
    suspend fun getGoals(): List<DomainUserGoal>
    suspend fun saveGoal(goal: DomainUserGoal)
    suspend fun completeGoal(goalId: String)
    suspend fun deleteGoal(goalId: String)

    // Quiz
    suspend fun saveQuizAttempt(attempt: DomainQuizAttempt)
    suspend fun getRecentQuizAttempts(): List<DomainQuizAttempt>

    // Versus
    suspend fun saveVersusMatch(match: DomainVersusMatch)
    suspend fun getRecentVersusMatches(): List<DomainVersusMatch>

    // Daily XP
    suspend fun getDailyXp(date: String): Int
    suspend fun addDailyXp(date: String, xp: Int)
    suspend fun getTotalDailyXp(): Int

    // Rank
    fun getRank(xp: Int): DomainRank

    // Observables
    fun observeStats(): Flow<DomainUserStats>
    fun observeGoals(): Flow<List<DomainUserGoal>>
    fun observeVersusMatches(): Flow<List<DomainVersusMatch>>
}
