package com.example.inscit.widget

import android.content.Context
import com.example.inscit.xp.Rank
import com.example.inscit.xp.XpManager
import java.time.LocalDate

/**
 * Single source of truth for widget data.
 *
 * Reads from the same SharedPreferences as MainActivity (inscit_prefs / user_data_json).
 * The previous implementation read from "widget_prefs" — a different file — so widgets
 * never displayed real user data. This fixes that data-source mismatch.
 */
object WidgetRepository {

    private const val PREFS_NAME = "inscit_prefs"
    private const val KEY_JSON = "user_data_json"
    private const val KEY_LEGACY = "user_data"

    /**
     * Loads widget data and returns a WidgetViewModel, or null if no user data exists.
     *
     * All derived values (rank, level, daysDone) are computed here so WidgetPainter
     * stays purely presentational.
     */
    fun loadViewModel(context: Context): WidgetViewModel? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_JSON, null)
            ?: prefs.getString(KEY_LEGACY, null)
            ?: return null

        val doc = com.example.inscit.parseUserDocumentJson(json) ?: return null

        val today = LocalDate.now()
        var daysDone = 0
        for (i in 0..6) {
            val date = today.minusDays((6 - i).toLong())
            if (doc.stats.completedChallengeDates.contains(date.toString())) {
                daysDone++
            }
        }

        val completedGoals = doc.goals.count { it.isCompleted }
        val rank = Rank.fromXp(doc.stats.xp)
        val level = XpManager.calculateLevel(doc.stats.xp)

        return WidgetViewModel(
            currentStreak = doc.stats.currentStreak,
            xp = doc.stats.xp,
            lastScore = doc.quizProgress.lastScore.toInt(),
            completedGoals = completedGoals,
            totalGoals = doc.goals.size,
            rankIcon = rank.iconAsset,
            rankLabel = rank.label,
            level = level,
            completedDates = doc.stats.completedChallengeDates,
            daysDone = daysDone
        )
    }
}
