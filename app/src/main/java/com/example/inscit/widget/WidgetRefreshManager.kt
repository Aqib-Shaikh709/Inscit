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
import com.example.inscit.data.repository.UserRepository
import com.example.inscit.data.repository.UserRepositoryImpl
import com.example.inscit.models.UserDocument
import com.example.inscit.xp.Rank
import com.example.inscit.xp.XpManager
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Central widget refresh manager.
 * Interconnects all 5 widgets (4 separate + 1 all-in-one) so they repaint together
 * whenever user data changes (XP, streak, goals, challenge, last score).
 */
object WidgetRefreshManager {

    private fun repository(context: Context): UserRepository = UserRepositoryImpl(context.applicationContext)

    /** Call this whenever user data changes (quiz end, goal complete, streak update, etc.) */
    fun refreshAllWidgets(context: Context) {
        if (!WidgetData.shouldUpdate(context)) return
        val mgr = AppWidgetManager.getInstance(context)
        paintAllWidgets(context, mgr)
        scheduleRefresh(context)
    }

    /** Conditional repaint: only paints when hash changed, but paints ALL widget types. */
    fun maybeRepaintAll(context: Context) {
        if (!WidgetData.shouldUpdate(context)) return
        val mgr = AppWidgetManager.getInstance(context)
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

        // ---------- Paint all-in-one widget ----------
        paintAllInOne(context, mgr, doc, stats, completedDates, today, done, completedGoals, totalGoals, rank, level, lastScore)

        // ---------- Paint 4 separate widgets ----------
        paintSeparateWidgets(context, mgr, doc, stats, completedDates, today, done, completedGoals, totalGoals, rank, level, lastScore)
    }

    private fun paintAllInOne(
        context: Context,
        mgr: AppWidgetManager,
        doc: UserDocument,
        stats: com.example.inscit.models.UserStats,
        completedDates: Set<String>,
        today: LocalDate,
        done: Int,
        completedGoals: Int,
        totalGoals: Int,
        rank: Rank,
        level: Int,
        lastScore: Int
    ) {
        val cn = ComponentName(context, InscitWidgetProvider::class.java)
        val ids = mgr.getAppWidgetIds(cn)
        if (ids.isEmpty()) return
        for (id in ids) {
            val rv = RemoteViews(context.packageName, R.layout.widget_inscit)
            rv.setTextViewText(R.id.streak_value, if (stats.currentStreak == 0) "❄️ ${stats.currentStreak}" else "🔥 ${stats.currentStreak}")
            rv.setTextViewText(R.id.streak_label, "STREAK")
            rv.setTextViewText(R.id.goals_title, "GOALS")
            rv.setTextViewText(R.id.goals_value, "$completedGoals/$totalGoals")
            rv.setTextViewText(R.id.rank_value, "${rank.icon} ${rank.label}")
            rv.setTextViewText(R.id.xp_value, "XP ${stats.xp}  Lvl $level")
            rv.setTextViewText(R.id.last_value, "Last $lastScore%")
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
            }
            rv.setOnClickPendingIntent(R.id.widget_root, WidgetData.mainIntent(context))
            rv.setOnClickPendingIntent(R.id.card_streak, WidgetData.sectionIntent(context, "streak"))
            rv.setOnClickPendingIntent(R.id.card_calendar, WidgetData.sectionIntent(context, "challenge"))
            rv.setOnClickPendingIntent(R.id.card_goals, WidgetData.sectionIntent(context, "goals"))
            rv.setOnClickPendingIntent(R.id.card_rank, WidgetData.sectionIntent(context, "xp"))
            mgr.updateAppWidget(id, rv)
        }
    }

    private fun paintSeparateWidgets(
        context: Context,
        mgr: AppWidgetManager,
        doc: UserDocument,
        stats: com.example.inscit.models.UserStats,
        completedDates: Set<String>,
        today: LocalDate,
        done: Int,
        completedGoals: Int,
        totalGoals: Int,
        rank: Rank,
        level: Int,
        lastScore: Int
    ) {
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
                        rv.setTextViewText(R.id.streak_value, if (stats.currentStreak == 0) "❄️ ${stats.currentStreak}" else "🔥 ${stats.currentStreak}")
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

    private fun scheduleRefresh(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WidgetRefreshReceiver::class.java).apply {
            action = "android.appwidget.action.APPWIDGET_UPDATE"
        }
        val pi = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 15 * 60 * 1000, 15 * 60 * 1000, pi)
    }

    fun cancelRefresh(context: Context) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, WidgetRefreshReceiver::class.java).apply {
            action = "android.appwidget.action.APPWIDGET_UPDATE"
        }
        val pi = PendingIntent.getBroadcast(context, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.cancel(pi)
    }
}
