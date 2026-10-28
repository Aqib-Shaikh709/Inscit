package com.example.inscit.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.inscit.R
import com.example.inscit.xp.Rank
import com.example.inscit.xp.XpManager
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class WidgetRefreshReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val mgr = AppWidgetManager.getInstance(context)
        Companion.maybeUpdateAll(context, mgr)
    }

    /** Central update: loads data once, only paints widgets when something changed. */
    fun maybeUpdateAll(context: Context, mgr: AppWidgetManager) {
        if (!WidgetData.shouldUpdate(context)) return
        paintAllWidgets(context, mgr)
    }

    private fun paintAllWidgets(context: Context, mgr: AppWidgetManager) {
        val doc = WidgetData.loadDoc(context) ?: return
        val stats = doc.stats
        val completedDates = stats.completedChallengeDates
        val today = LocalDate.now()
        var done = 0
        for (i in 0..6) {
            val date = today.minusDays((6 - i).toLong())
            if (completedDates.contains(date.toString())) done++
        }
        val completedGoals = doc.goals.count { it.isCompleted }
        val totalGoals = doc.goals.size
        val rank = Rank.fromXp(stats.xp)
        val level = XpManager.calculateLevel(stats.xp)
        val lastScore = doc.quizProgress.lastScore.toInt()

        val specs = mapOf(
            StreakWidgetProvider::class.java to R.layout.widget_streak,
            ChallengeWidgetProvider::class.java to R.layout.widget_challenge,
            GoalsWidgetProvider::class.java to R.layout.widget_goals,
            XpWidgetProvider::class.java to R.layout.widget_xp
        )

        for ((clazz, layoutId) in specs) {
            val cn = ComponentName(context, clazz)
            val ids = mgr.getAppWidgetIds(cn)
            if (ids.isEmpty()) continue
            for (id in ids) {
                val rv = RemoteViews(context.packageName, layoutId)
                when (clazz) {
                    StreakWidgetProvider::class.java -> {
                        rv.setTextViewText(R.id.streak_value, "🔥 ${stats.currentStreak}")
                        rv.setTextViewText(R.id.streak_label, "STREAK")
                        rv.setOnClickPendingIntent(R.id.widget_root, WidgetData.mainIntent(context))
                        rv.setOnClickPendingIntent(R.id.streak_value, WidgetData.sectionIntent(context, "streak"))
                        rv.setOnClickPendingIntent(R.id.streak_label, WidgetData.sectionIntent(context, "streak"))
                    }
                    ChallengeWidgetProvider::class.java -> {
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
                    }
                    GoalsWidgetProvider::class.java -> {
                        rv.setTextViewText(R.id.goals_title, "GOALS")
                        rv.setTextViewText(R.id.goals_value, "$completedGoals/$totalGoals")
                        rv.setOnClickPendingIntent(R.id.widget_root, WidgetData.mainIntent(context))
                        rv.setOnClickPendingIntent(R.id.goals_value, WidgetData.sectionIntent(context, "goals"))
                        rv.setOnClickPendingIntent(R.id.goals_title, WidgetData.sectionIntent(context, "goals"))
                    }
                    XpWidgetProvider::class.java -> {
                        rv.setTextViewText(R.id.rank_value, "${rank.icon} ${rank.label}")
                        rv.setTextViewText(R.id.xp_value, "XP ${stats.xp}  Lvl $level")
                        rv.setTextViewText(R.id.last_value, "Last $lastScore%")
                        rv.setOnClickPendingIntent(R.id.widget_root, WidgetData.mainIntent(context))
                        rv.setOnClickPendingIntent(R.id.rank_value, WidgetData.sectionIntent(context, "xp"))
                        rv.setOnClickPendingIntent(R.id.xp_value, WidgetData.sectionIntent(context, "xp"))
                        rv.setOnClickPendingIntent(R.id.last_value, WidgetData.sectionIntent(context, "xp"))
                    }
                }
                mgr.updateAppWidget(id, rv)
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

    companion object {
        fun refreshOnAppOpen(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            maybeUpdateAll(context, mgr)
            WidgetRefreshReceiver().scheduleRefresh(context)
        }

        /** Static entry point used by each provider's onUpdate. */
        fun maybeUpdateAll(context: Context, mgr: AppWidgetManager) {
            WidgetRefreshReceiver().maybeUpdateAll(context, mgr)
        }
    }
}
