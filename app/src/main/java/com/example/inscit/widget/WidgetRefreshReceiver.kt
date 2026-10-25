package com.example.inscit.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.inscit.MainActivity
import com.example.inscit.R
import com.example.inscit.xp.Rank
import com.example.inscit.xp.XpManager
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class WidgetRefreshReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: android.content.Intent?) {
        val mgr = AppWidgetManager.getInstance(context)
        val specs = mapOf(
            StreakWidgetProvider::class.java to "streak",
            ChallengeWidgetProvider::class.java to "challenge",
            GoalsWidgetProvider::class.java to "goals",
            XpWidgetProvider::class.java to "xp"
        )
        for ((clazz, section) in specs) {
            val cn = ComponentName(context, clazz)
            val ids = mgr.getAppWidgetIds(cn)
            if (ids.isEmpty()) continue
            val doc = loadDoc(context)
            val stats = doc?.stats
            when (clazz) {
                StreakWidgetProvider::class.java -> {
                    for (id in ids) {
                        val rv = RemoteViews(context.packageName, R.layout.widget_streak)
                        rv.setTextViewText(R.id.streak_value, "🔥 ${stats?.currentStreak ?: 0}")
                        rv.setOnClickPendingIntent(R.id.widget_root, mainIntent(context))
                        rv.setOnClickPendingIntent(R.id.streak_value, sectionIntent(context, section))
                        rv.setOnClickPendingIntent(R.id.streak_label, sectionIntent(context, section))
                        mgr.updateAppWidget(id, rv)
                    }
                }
                ChallengeWidgetProvider::class.java -> {
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
                            val letter = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()).take(1).uppercase()
                            rv.setTextViewText(dotId, letter)
                            rv.setInt(dotId, "setBackgroundResource", if (isDone) R.drawable.day_done else R.drawable.day_pending)
                            rv.setOnClickPendingIntent(dotId, sectionIntent(context, section))
                        }
                        rv.setOnClickPendingIntent(R.id.widget_root, mainIntent(context))
                        rv.setOnClickPendingIntent(R.id.cal_title, sectionIntent(context, section))
                        rv.setOnClickPendingIntent(R.id.cal_sub, sectionIntent(context, section))
                        mgr.updateAppWidget(id, rv)
                    }
                }
                GoalsWidgetProvider::class.java -> {
                    val completed = doc?.goals?.count { it.isCompleted } ?: 0
                    val total = doc?.goals?.size ?: 0
                    for (id in ids) {
                        val rv = RemoteViews(context.packageName, R.layout.widget_goals)
                        rv.setTextViewText(R.id.goals_title, "GOALS")
                        rv.setTextViewText(R.id.goals_value, "$completed/$total")
                        rv.setOnClickPendingIntent(R.id.widget_root, mainIntent(context))
                        rv.setOnClickPendingIntent(R.id.goals_value, sectionIntent(context, section))
                        rv.setOnClickPendingIntent(R.id.goals_title, sectionIntent(context, section))
                        mgr.updateAppWidget(id, rv)
                    }
                }
                XpWidgetProvider::class.java -> {
                    val rank = com.example.inscit.xp.Rank.fromXp(stats?.xp ?: 0)
                    val level = com.example.inscit.xp.XpManager.calculateLevel(stats?.xp ?: 0)
                    for (id in ids) {
                        val rv = RemoteViews(context.packageName, R.layout.widget_xp)
                        rv.setTextViewText(R.id.rank_value, "${rank.icon} ${rank.label}")
                        rv.setTextViewText(R.id.xp_value, "XP ${stats?.xp ?: 0}  Lvl $level")
                        rv.setTextViewText(R.id.last_value, "Last ${(doc?.quizProgress?.lastScore ?: 0f).toInt()}%")
                        rv.setOnClickPendingIntent(R.id.widget_root, mainIntent(context))
                        rv.setOnClickPendingIntent(R.id.rank_value, sectionIntent(context, section))
                        rv.setOnClickPendingIntent(R.id.xp_value, sectionIntent(context, section))
                        rv.setOnClickPendingIntent(R.id.last_value, sectionIntent(context, section))
                        mgr.updateAppWidget(id, rv)
                    }
                }
            }
        }
    }

    fun scheduleRefresh(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WidgetRefreshReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 15 * 60 * 1000, 15 * 60 * 1000, pi)
    }

    fun cancelRefresh(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WidgetRefreshReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.cancel(pi)
    }

    private fun loadDoc(context: Context): com.example.inscit.models.UserDocument? {
        return try {
            val prefs = context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
            val json = prefs.getString("user_data_json", null) ?: return null
            com.example.inscit.parseUserDocumentJson(json)
        } catch (_: Exception) { null }
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

    companion object {
        fun refreshOnAppOpen(context: Context) {
            val receiver = WidgetRefreshReceiver()
            val intent = android.content.Intent(context, WidgetRefreshReceiver::class.java)
            receiver.onReceive(context, intent)
            receiver.scheduleRefresh(context)
        }
    }
}
