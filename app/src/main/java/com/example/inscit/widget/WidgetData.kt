package com.example.inscit.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.inscit.MainActivity
import com.example.inscit.models.UserDocument
import com.example.inscit.parseUserDocumentJson

object WidgetData {
    private const val PREFS = "widget_prefs"
    private const val HASH_KEY = "widget_data_hash"

    fun loadDoc(context: Context): UserDocument? {
        return try {
            val prefs = context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
            val json = prefs.getString("user_data_json", null) ?: return null
            parseUserDocumentJson(json)
        } catch (_: Exception) {
            null
        }
    }

    /** Hash of every field the widgets render. Returns null if no data. */
    fun computeHash(doc: UserDocument): String {
        val sortedDates = doc.stats.completedChallengeDates.sorted().joinToString(",")
        val completedGoals = doc.goals.count { it.isCompleted }
        return buildString {
            append(doc.stats.currentStreak).append("|")
            append(doc.stats.xp).append("|")
            append(sortedDates).append("|")
            append(doc.goals.size).append("|")
            append(completedGoals).append("|")
            append(doc.quizProgress.lastScore)
        }
    }

    /**
     * Returns true only when rendered data has changed since last check.
     * Stores the new hash so repeated checks are no-ops until data changes.
     */
    fun shouldUpdate(context: Context): Boolean {
        val doc = loadDoc(context) ?: return false
        val current = computeHash(doc)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = prefs.getString(HASH_KEY, null)
        if (current == stored) return false
        prefs.edit().putString(HASH_KEY, current).apply()
        return true
    }

    fun mainIntent(context: Context): PendingIntent {
        val i = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(context, 2001, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun sectionIntent(context: Context, section: String): PendingIntent {
        val i = Intent(context, MainActivity::class.java).apply {
            action = "com.example.inscit.WIDGET_SECTION"
            putExtra("widget_section", section)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(context, section.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
