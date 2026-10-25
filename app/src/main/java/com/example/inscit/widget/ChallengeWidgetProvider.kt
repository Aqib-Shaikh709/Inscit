package com.example.inscit.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.inscit.MainActivity
import com.example.inscit.R
import com.example.inscit.models.UserDocument
import com.example.inscit.parseUserDocumentJson
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class ChallengeWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateSelf(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateSelf(context: Context, mgr: AppWidgetManager, ids: IntArray) {
            val doc = loadDoc(context)
            val completedDates = doc?.stats?.completedChallengeDates ?: emptySet()
            val today = LocalDate.now()
            var done = 0
            for (i in 0..6) {
                val date = today.minusDays((6 - i).toLong())
                val iso = date.toString()
                if (completedDates.contains(iso)) done++
            }
            for (id in ids) {
                val rv = RemoteViews(context.packageName, R.layout.widget_challenge)
                rv.setTextViewText(R.id.cal_title, "DAILY CHALLENGE")
                rv.setTextViewText(R.id.cal_sub, "$done/7 days")
                for (i in 0..6) {
                    val date = today.minusDays((6 - i).toLong())
                    val iso = date.toString()
                    val dotId = context.resources.getIdentifier("cd$i", "id", context.packageName)
                    val isDone = completedDates.contains(iso)
                    val letter = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1).uppercase()
                    rv.setTextViewText(dotId, letter)
                    rv.setInt(dotId, "setBackgroundResource", if (isDone) R.drawable.day_done else R.drawable.day_pending)
                    rv.setOnClickPendingIntent(dotId, WidgetData.sectionIntent(context, "challenge"))
                }
                rv.setOnClickPendingIntent(R.id.widget_root, WidgetData.mainIntent(context))
                rv.setOnClickPendingIntent(R.id.cal_title, WidgetData.sectionIntent(context, "challenge"))
                rv.setOnClickPendingIntent(R.id.cal_sub, WidgetData.sectionIntent(context, "challenge"))
                mgr.updateAppWidget(id, rv)
            }
        }

        private fun loadDoc(context: Context): UserDocument? {
            return try {
                val prefs = context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
                val json = prefs.getString("user_data_json", null) ?: return null
                parseUserDocumentJson(json)
            } catch (_: Exception) { null }
        }
    }
}
