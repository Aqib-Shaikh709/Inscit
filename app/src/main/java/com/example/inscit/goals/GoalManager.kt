package com.example.inscit.goals

import android.content.Context
import com.example.inscit.models.GoalType
import com.example.inscit.models.UserDocument
import com.example.inscit.models.UserGoal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object GoalManager {

    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun todayXp(doc: UserDocument): Int = doc.dailyXp[today()] ?: 0

    fun activeGoals(doc: UserDocument): List<UserGoal> = doc.goals.filter { !it.isCompleted }

    fun createXpGoal(
        doc: UserDocument,
        title: String,
        targetXp: Int,
        dailyTarget: Int
    ): UserDocument {
        val cleanTarget = targetXp.coerceAtLeast(10)
        val goal = UserGoal(
            id = UUID.randomUUID().toString(),
            title = sanitizeTitle(title).ifBlank { "Earn $cleanTarget XP" },
            type = GoalType.XP,
            targetValue = cleanTarget,
            dailyTarget = dailyTarget.coerceAtLeast(0)
        )
        return doc.copy(goals = doc.goals + goal)
    }

    fun createQuizGoal(
        doc: UserDocument,
        title: String,
        quizCount: Int,
        scoreThreshold: Int
    ): UserDocument {
        val cleanCount = quizCount.coerceAtLeast(1)
        val cleanThreshold = scoreThreshold.coerceIn(1, 100)
        val goal = UserGoal(
            id = UUID.randomUUID().toString(),
            title = sanitizeTitle(title).ifBlank { "Score $cleanThreshold%+ in $cleanCount quizzes" },
            type = GoalType.QUIZ,
            targetValue = cleanCount,
            scoreThreshold = cleanThreshold
        )
        return doc.copy(goals = doc.goals + goal)
    }

    fun deleteGoal(doc: UserDocument, goalId: String): UserDocument =
        doc.copy(goals = doc.goals.filterNot { it.id == goalId })

    /** Removes goals that were completed more than one day ago. */
    fun pruneExpiredCompletedGoals(
        doc: UserDocument,
        now: Long = System.currentTimeMillis()
    ): UserDocument {
        val oneDayMs = 24L * 60 * 60 * 1000
        val hasExpired = doc.goals.any { it.isCompleted && now - it.completedAt > oneDayMs }
        if (!hasExpired) return doc
        return doc.copy(
            goals = doc.goals.filterNot { it.isCompleted && now - it.completedAt > oneDayMs }
        )
    }

    fun updateIsCompleted(doc: UserDocument, goalId: String): UserDocument =
        doc.copy(goals = doc.goals.map {
            if (it.id == goalId) it.copy(isCompleted = true, completedAt = System.currentTimeMillis()) else it
        })

    /** Returns updated doc + the goals that just crossed their finish line. */
    fun applyQuizResult(
        doc: UserDocument,
        xpEarned: Int,
        score: Float
    ): Pair<UserDocument, List<UserGoal>> {
        val pruned = pruneExpiredCompletedGoals(doc)
        var current = pruned
        val completed = mutableListOf<UserGoal>()

        if (xpEarned > 0) {
            val map = current.dailyXp.toMutableMap()
            val key = today()
            map[key] = (map[key] ?: 0) + xpEarned
            current = current.copy(dailyXp = map)
        }

        val resultGoals = current.goals.map { goal ->
            var updated = goal
            when (goal.type) {
                GoalType.XP -> {
                    if (!goal.isCompleted && xpEarned > 0) {
                        updated = goal.copy(currentValue = goal.currentValue + xpEarned)
                    }
                }
                GoalType.QUIZ -> {
                    if (!goal.isCompleted && score >= goal.scoreThreshold) {
                        updated = goal.copy(currentValue = goal.currentValue + 1)
                    }
                }
            }
            if (!goal.isCompleted && updated.currentValue >= updated.targetValue) {
                updated = updated.copy(isCompleted = true, currentValue = minOf(updated.currentValue, updated.targetValue), completedAt = System.currentTimeMillis())
                completed += updated
            }
            updated
        }
        return current.copy(goals = resultGoals) to completed
    }

    // ---------- Persistence parsing for background workers ----------

    fun loadGoalsFromPrefs(context: Context): List<UserGoal> {
        return try {
            val prefs = context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
            val data = prefs.getString("user_data", null) ?: return emptyList()
            parseGoalsFromData(data)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadDailyXpFromPrefs(context: Context): Map<String, Int> {
        return try {
            val prefs = context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
            val data = prefs.getString("user_data", null) ?: return emptyMap()
            parseDailyXpFromData(data)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // Goal field 16 is wrapped with escapePipe in serialization; titles are sanitized
    // so goals never contain raw separators ("~", ";;", "|") and are safe to parse raw.
    fun parseGoalsFromData(data: String): List<UserGoal> {
        val parts = data.split("|")
        if (parts.size <= 16) return emptyList()
        return unescapePipeSafe(parts[16]).split(";;").mapNotNull { raw ->
            val seg = raw.split("~")
            if (seg.size < 10) return@mapNotNull null
            try {
                UserGoal(
                    id = seg[0],
                    title = seg[1],
                    type = if (seg[2] == "QUIZ") GoalType.QUIZ else GoalType.XP,
                    targetValue = seg[3].toInt(),
                    currentValue = seg[4].toInt(),
                    scoreThreshold = seg[5].toInt(),
                    dailyTarget = seg[6].toInt(),
                    isCompleted = seg[7].toBoolean(),
                    createdAt = seg[8].toLong(),
                    completedAt = seg[9].toLong()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun parseDailyXpFromData(data: String): Map<String, Int> {
        val parts = data.split("|")
        if (parts.size <= 17) return emptyMap()
        return unescapePipeSafe(parts[17]).split("|").mapNotNull { raw ->
            val seg = raw.split(":")
            if (seg.size == 2) runCatching { seg[0] to seg[1].toInt() }.getOrNull() else null
        }.toMap()
    }

    fun sanitizeTitle(title: String): String =
        title.replace("|", " ").replace("~", " ").replace(";", " ").replace("\\", " ").trim()

    private fun unescapePipeSafe(s: String): String =
        s.replace("\\|", "|").replace("\\\\", "\\")
}