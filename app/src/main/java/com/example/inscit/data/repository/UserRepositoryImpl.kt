package com.example.inscit.data.repository

import android.content.Context
import com.example.inscit.data.room.AppDatabase
import com.example.inscit.data.room.converter.Converters
import com.example.inscit.data.room.dao.DailyXpDao
import com.example.inscit.data.room.dao.QuizAttemptDao
import com.example.inscit.data.room.dao.UserGoalDao
import com.example.inscit.data.room.dao.UserProfileDao
import com.example.inscit.data.room.dao.UserStatsDao
import com.example.inscit.data.room.dao.VersusMatchDao
import com.example.inscit.data.room.entity.DailyXpEntity
import com.example.inscit.data.room.entity.UserGoalEntity
import com.example.inscit.data.room.entity.UserProfileEntity
import com.example.inscit.data.room.entity.UserStatsEntity
import com.example.inscit.data.room.entity.QuizAttemptEntity
import com.example.inscit.data.room.entity.VersusMatchEntity
import com.example.inscit.domain.model.DomainQuizAttempt
import com.example.inscit.domain.model.DomainUserProfile
import com.example.inscit.domain.model.DomainUserStats
import com.example.inscit.domain.model.DomainUserGoal
import com.example.inscit.domain.model.DomainVersusMatch
import com.example.inscit.domain.model.DomainRank
import com.example.inscit.domain.model.GoalType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepositoryImpl(
    private val context: Context,
    private val profileDao: UserProfileDao,
    private val statsDao: UserStatsDao,
    private val goalDao: UserGoalDao,
    private val quizDao: QuizAttemptDao,
    private val versusDao: VersusMatchDao,
    private val dailyXpDao: DailyXpDao
) : UserRepository {

    override suspend fun getProfile(): DomainUserProfile? {
        val entity = profileDao.getProfile()
        return entity?.toDomain()
    }

    override suspend fun saveProfile(profile: DomainUserProfile) {
        val entity = profile.toEntity()
        profileDao.insert(entity)
    }

    override suspend fun updateLoginTime() {
        val entity = profileDao.getProfile()
        if (entity != null) {
            profileDao.update(entity.copy(lastLoginAt = System.currentTimeMillis()))
        }
    }

    override suspend fun getStats(): DomainUserStats {
        val entity = statsDao.getStats()
        return entity?.toDomain() ?: DomainUserStats()
    }

    override suspend fun updateStats(stats: DomainUserStats) {
        val entity = stats.toEntity()
        statsDao.insert(entity)
    }

    override suspend fun incrementXp(xp: Int) {
        val current = statsDao.getStats() ?: UserStatsEntity()
        val newXp = current.xp + xp
        statsDao.insert(current.copy(xp = newXp, level = (newXp / 100) + 1))
    }

    override suspend fun updateStreak(score: Float) {
        val current = statsDao.getStats() ?: UserStatsEntity()
        val today = java.time.LocalDate.now().toString()
        val newStreak = when {
            score >= 80f -> if (current.lastActivityDate == today) current.currentStreak else current.currentStreak + 1
            score >= 60f -> current.currentStreak
            else -> if (current.lastActivityDate == today) current.currentStreak else 0
        }
        val newXp = current.xp + when { score >= 80f -> 10; score >= 60f -> 5; else -> 0 }
        statsDao.insert(current.copy(
            currentStreak = newStreak,
            lastActivityDate = today,
            xp = newXp,
            level = (newXp / 100) + 1,
            quizzesTaken = current.quizzesTaken + 1
        ))
    }

    override suspend fun getGoals(): List<DomainUserGoal> {
        return goalDao.getGoals().first().map { it.toDomain() }
    }

    override suspend fun saveGoal(goal: DomainUserGoal) {
        val entity = goal.toEntity()
        goalDao.insert(entity)
    }

    override suspend fun completeGoal(goalId: String) {
        val goal = goalDao.getGoalById(goalId)
        if (goal != null && !goal.isCompleted) {
            goalDao.update(goal.copy(isCompleted = true, completedAt = System.currentTimeMillis()))
        }
    }

    override suspend fun deleteGoal(goalId: String) {
        val goal = goalDao.getGoalById(goalId)
        if (goal != null) {
            goalDao.delete(goal)
        }
    }

    override suspend fun saveQuizAttempt(attempt: DomainQuizAttempt) {
        val entity = attempt.toEntity()
        quizDao.insert(entity)
    }

    override suspend fun getRecentQuizAttempts(): List<DomainQuizAttempt> {
        return quizDao.getAttempts().map { it.toDomain() }
    }

    override suspend fun saveVersusMatch(match: DomainVersusMatch) {
        val entity = match.toEntity()
        versusDao.insert(entity)
    }

    override suspend fun getRecentVersusMatches(): List<DomainVersusMatch> {
        return versusDao.getMatches().map { it.toDomain() }
    }

    override suspend fun getDailyXp(date: String): Int {
        return dailyXpDao.getXpForDate(date) ?: 0
    }

    override suspend fun addDailyXp(date: String, xp: Int) {
        val current = dailyXpDao.getXpForDate(date) ?: 0
        dailyXpDao.insert(DailyXpEntity(date = date, xp = current + xp))
    }

    override suspend fun getTotalDailyXp(): Int {
        return dailyXpDao.getTotalDailyXp() ?: 0
    }

    override fun getRank(xp: Int): DomainRank {
        return when {
            xp >= 2000 -> DomainRank("Legend", "👑", 2000)
            xp >= 1500 -> DomainRank("Hero", "🦸‍♂️", 1500)
            xp >= 1000 -> DomainRank("Master", "♟️", 1000)
            xp >= 600 -> DomainRank("Genius", "🧠", 600)
            xp >= 300 -> DomainRank("Scientist", "🧪", 300)
            xp >= 150 -> DomainRank("Explorer", "🚀", 150)
            else -> DomainRank("Beginner", "🌱", 0)
        }
    }

    override fun observeStats(): Flow<DomainUserStats> {
        return flow {
            while (true) {
                emit(getStats())
                kotlinx.coroutines.delay(2000)
            }
        }
    }

    override fun observeGoals(): Flow<List<DomainUserGoal>> {
        return goalDao.getGoals().map { it.map { it.toDomain() } }
    }

    override fun observeVersusMatches(): Flow<List<DomainVersusMatch>> {
        return versusDao.getRecentMatches().map { it.map { it.toDomain() } }
    }
}

// ---- Entity <-> Domain Mappers ----

private fun UserProfileEntity.toDomain(): DomainUserProfile =
    DomainUserProfile(
        userId = userId,
        name = name,
        email = email,
        createdAt = createdAt,
        photoUrl = photoUrl,
        theme = theme,
        language = language,
        notificationsEnabled = notificationsEnabled,
        lastLoginAt = lastLoginAt
    )

private fun DomainUserProfile.toEntity(): UserProfileEntity =
    UserProfileEntity(
        userId = userId,
        name = name,
        email = email,
        createdAt = createdAt,
        photoUrl = photoUrl,
        theme = theme,
        language = language,
        notificationsEnabled = notificationsEnabled,
        lastLoginAt = lastLoginAt
    )

private fun UserStatsEntity.toDomain(): DomainUserStats =
    DomainUserStats(
        xp = xp,
        level = level,
        quizzesTaken = quizzesTaken,
        completedChallengeDates = completedChallengeDates.split(",").filter { it.isNotEmpty() }.toSet(),
        totalUsageTime = totalUsageTime,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastActivityDate = lastActivityDate,
        lastScore = lastScore
    )

private fun DomainUserStats.toEntity(): UserStatsEntity =
    UserStatsEntity(
        xp = xp,
        level = level,
        quizzesTaken = quizzesTaken,
        completedChallengeDates = completedChallengeDates.joinToString(","),
        totalUsageTime = totalUsageTime,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        lastActivityDate = lastActivityDate,
        lastScore = lastScore,
        dailyXp = Converters.json.encodeToString(emptyMap())
    )

private fun UserGoalEntity.toDomain(): DomainUserGoal =
    DomainUserGoal(
        id = id,
        title = title,
        type = if (type == "XP") GoalType.XP else GoalType.QUIZ,
        targetValue = targetValue,
        currentValue = currentValue,
        scoreThreshold = scoreThreshold,
        dailyTarget = dailyTarget,
        isCompleted = isCompleted,
        createdAt = createdAt,
        completedAt = completedAt
    )

private fun DomainUserGoal.toEntity(): UserGoalEntity =
    UserGoalEntity(
        id = id,
        title = title,
        type = type.name,
        targetValue = targetValue,
        currentValue = currentValue,
        scoreThreshold = scoreThreshold,
        dailyTarget = dailyTarget,
        isCompleted = isCompleted,
        createdAt = createdAt,
        completedAt = completedAt
    )

private fun QuizAttemptEntity.toDomain(): DomainQuizAttempt =
    DomainQuizAttempt(
        attemptId = attemptId,
        score = score,
        xpEarned = xpEarned,
        timestamp = timestamp,
        domainScores = Converters.json.decodeFromString(domainScores),
        strengths = Converters.json.decodeFromString(strengths),
        weaknesses = Converters.json.decodeFromString(weaknesses)
    )

private fun DomainQuizAttempt.toEntity(): QuizAttemptEntity =
    QuizAttemptEntity(
        attemptId = attemptId,
        score = score,
        xpEarned = xpEarned,
        timestamp = timestamp,
        domainScores = Converters.json.encodeToString(domainScores),
        strengths = Converters.json.encodeToString(strengths),
        weaknesses = Converters.json.encodeToString(weaknesses)
    )

private fun VersusMatchEntity.toDomain(): DomainVersusMatch =
    DomainVersusMatch(
        matchId = matchId,
        opponentName = opponentName,
        isHost = isHost,
        hostScore = hostScore,
        guestScore = guestScore,
        result = result,
        timestamp = timestamp,
        questionsHash = questionsHash
    )

private fun DomainVersusMatch.toEntity(): VersusMatchEntity =
    VersusMatchEntity(
        matchId = matchId,
        opponentName = opponentName,
        isHost = isHost,
        hostScore = hostScore,
        guestScore = guestScore,
        result = result,
        timestamp = timestamp,
        questionsHash = questionsHash
    )
