package com.example.inscit.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.inscit.MainActivity
import com.example.inscit.R
import com.example.inscit.parseUserDocumentJson
import com.example.inscit.xp.Rank
import com.example.inscit.xp.XpManager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class InscitWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleRefresh(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelRefresh(context)
    }

    companion object {
        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val cn = ComponentName(context, InscitWidgetProvider::class.java)
            val ids = mgr.getAppWidgetIds(cn)
            for (id in ids) {
                updateWidget(context, mgr, id)
            }
        }

        fun refreshOnAppOpen(context: Context) {
            updateAll(context)
            scheduleRefresh(context)
        }

        private fun scheduleRefresh(context: Context) {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, InscitWidgetProvider::class.java).apply {
                action = "android.appwidget.action.APPWIDGET_UPDATE"
            }
            val pi = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 15 * 60 * 1000, 15 * 60 * 1000, pi)
        }

        private fun cancelRefresh(context: Context) {
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, InscitWidgetProvider::class.java).apply {
                action = "android.appwidget.action.APPWIDGET_UPDATE"
            }
            val pi = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarm.cancel(pi)
        }

        private fun loadDoc(context: Context): com.example.inscit.models.UserDocument? {
            return try {
                val prefs = context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
                val json = prefs.getString("user_data_json", null) ?: return null
                parseUserDocumentJson(json)
            } catch (_: Exception) {
                null
            }
        }

        private fun sectionIntent(context: Context, section: String): PendingIntent {
            val i = Intent(context, MainActivity::class.java).apply {
                action = "com.example.inscit.WIDGET_SECTION"
                putExtra("widget_section", section)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return PendingIntent.getActivity(context, section.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        private fun mainIntent(context: Context): PendingIntent {
            val i = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            return PendingIntent.getActivity(context, 2001, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        private fun updateWidget(context: Context, mgr: AppWidgetManager, id: Int) {
            val rv = RemoteViews(context.packageName, R.layout.widget_inscit)
            val doc = loadDoc(context)
            val stats = doc?.stats

            rv.setTextViewText(R.id.streak_value, "🔥 ${stats?.currentStreak ?: 0}")
            rv.setTextViewText(R.id.streak_label, "STREAK")

            val completed = doc?.goals?.count { it.isCompleted } ?: 0
            val total = doc?.goals?.size ?: 0
            rv.setTextViewText(R.id.goals_value, "$completed/$total")
            rv.setTextViewText(R.id.goals_title, "GOALS")

            val rank = Rank.fromXp(stats?.xp ?: 0)
            rv.setTextViewText(R.id.rank_value, "${rank.icon} ${rank.label}")
            val level = XpManager.calculateLevel(stats?.xp ?: 0)
            rv.setTextViewText(R.id.xp_value, "XP ${stats?.xp ?: 0}  Lvl $level")
            rv.setTextViewText(R.id.last_value, "Last ${(doc?.quizProgress?.lastScore ?: 0f).toInt()}%")

            val completedDates = doc?.stats?.completedChallengeDates ?: emptySet()
            val today = LocalDate.now()
            var done = 0
            for (i in 0..6) {
                val date = today.minusDays((6 - i).toLong())
                val iso = date.toString()
                val dotId = context.resources.getIdentifier("cd$i", "id", context.packageName)
                val isDone = completedDates.contains(iso)
                if (isDone) done++
                val letter = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(1).uppercase()
                rv.setTextViewText(dotId, letter)
                rv.setInt(dotId, "setBackgroundResource", if (isDone) R.drawable.day_done else R.drawable.day_pending)
            }
            rv.setTextViewText(R.id.cal_title, "DAILY CHALLENGE")
            rv.setTextViewText(R.id.cal_sub, "$done/7 days")

            rv.setOnClickPendingIntent(R.id.widget_root, mainIntent(context))
            rv.setOnClickPendingIntent(R.id.card_streak, sectionIntent(context, "streak"))
            rv.setOnClickPendingIntent(R.id.card_calendar, sectionIntent(context, "challenge"))
            rv.setOnClickPendingIntent(R.id.card_goals, sectionIntent(context, "goals"))
            rv.setOnClickPendingIntent(R.id.card_rank, sectionIntent(context, "xp"))

            mgr.updateAppWidget(id, rv)
        }
    }
}
