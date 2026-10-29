package com.example.inscit.data.repository

import android.content.Context
import com.example.inscit.models.UserDocument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Single source of truth for all user data access.
 * Replaces direct SharedPreferences reads scattered across widgets, workers, and UI.
 */
interface UserRepository {
    fun getDocument(): UserDocument?
    fun saveDocument(doc: UserDocument)
    fun observeDocument(): Flow<UserDocument>
    fun updateStats(block: (com.example.inscit.models.UserStats) -> com.example.inscit.models.UserStats)
    fun updateGoals(block: (List<com.example.inscit.models.UserGoal>) -> List<com.example.inscit.models.UserGoal>)
    fun updateQuizProgress(block: (com.example.inscit.models.QuizProgress) -> com.example.inscit.models.QuizProgress)
    fun updateChallengeStatus(block: (com.example.inscit.models.DailyChallengeStatus) -> com.example.inscit.models.DailyChallengeStatus)
    fun updateDailyXp(date: String, xp: Int)
    fun clear()
}

class UserRepositoryImpl(private val context: Context) : UserRepository {
    private val PREFS_NAME = "inscit_prefs"
    private val KEY_JSON = "user_data_json"
    private val KEY_LEGACY = "user_data"

    private fun prefs() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getDocument(): UserDocument? {
        return try {
            val json = prefs().getString(KEY_JSON, null)
                ?: prefs().getString(KEY_LEGACY, null)
                ?: return null
            com.example.inscit.parseUserDocumentJson(json)
        } catch (_: Exception) { null }
    }

    override fun saveDocument(doc: UserDocument) {
        try {
            val json = com.example.inscit.serializeUserDocumentJson(doc)
            prefs().edit().putString(KEY_JSON, json).apply()
        } catch (_: Exception) {}
    }

    override fun observeDocument(): Flow<UserDocument> = flow {
        while (true) {
            val doc = getDocument()
            if (doc != null) emit(doc)
            kotlinx.coroutines.delay(2000)
        }
    }

    override fun updateStats(block: (com.example.inscit.models.UserStats) -> com.example.inscit.models.UserStats) {
        getDocument()?.let { doc ->
            saveDocument(doc.copy(stats = block(doc.stats)))
        }
    }

    override fun updateGoals(block: (List<com.example.inscit.models.UserGoal>) -> List<com.example.inscit.models.UserGoal>) {
        getDocument()?.let { doc ->
            saveDocument(doc.copy(goals = block(doc.goals)))
        }
    }

    override fun updateQuizProgress(block: (com.example.inscit.models.QuizProgress) -> com.example.inscit.models.QuizProgress) {
        getDocument()?.let { doc ->
            saveDocument(doc.copy(quizProgress = block(doc.quizProgress)))
        }
    }

    override fun updateChallengeStatus(block: (com.example.inscit.models.DailyChallengeStatus) -> com.example.inscit.models.DailyChallengeStatus) {
        getDocument()?.let { doc ->
            saveDocument(doc.copy(dailyChallengeStatus = block(doc.dailyChallengeStatus)))
        }
    }

    override fun updateDailyXp(date: String, xp: Int) {
        getDocument()?.let { doc ->
            val map = doc.dailyXp.toMutableMap()
            map[date] = (map[date] ?: 0) + xp
            saveDocument(doc.copy(dailyXp = map))
        }
    }

    override fun clear() {
        prefs().edit().remove(KEY_JSON).remove(KEY_LEGACY).apply()
    }
}
