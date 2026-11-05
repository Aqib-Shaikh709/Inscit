package com.example.inscit.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.inscit.MainActivity
import com.example.inscit.R

/**
 * Paints widget RemoteViews from a WidgetViewModel.
 *
 * Uses direct view ID references (R.id.*) — no fragile getIdentifier() string lookups.
 * One method per widget type. No provider lifecycle logic lives here.
 */
object WidgetPainter {

    private const val REQUEST_MAIN = 1001
    private const val REQUEST_STREAK_SECTION = 2001
    private const val REQUEST_CHALLENGE_SECTION = 2002
    private const val REQUEST_GOALS_SECTION = 2003
    private const val REQUEST_XP_SECTION = 2004

    private val DAY_IDS = intArrayOf(R.id.cd0, R.id.cd1, R.id.cd2, R.id.cd3, R.id.cd4, R.id.cd5, R.id.cd6)
    private val DAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")

    /** Common main widget click: opens the app. */
    fun setMainIntent(context: Context, rv: RemoteViews, viewId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, REQUEST_MAIN, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        rv.setOnClickPendingIntent(viewId, pending)
    }

    /** Section click: opens a specific settings section. Stable request codes per section. */
    fun setSectionIntent(context: Context, rv: RemoteViews, viewId: Int, section: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.inscit.WIDGET_SECTION"
            putExtra("widget_section", section)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val requestCode = when (section) {
            "streak" -> REQUEST_STREAK_SECTION
            "challenge" -> REQUEST_CHALLENGE_SECTION
            "goals" -> REQUEST_GOALS_SECTION
            "xp" -> REQUEST_XP_SECTION
            else -> REQUEST_MAIN
        }
        val pending = PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        rv.setOnClickPendingIntent(viewId, pending)
    }

    /** Paints the all-in-one bento widget (4 cards in a grid). */
    fun paintAllInOne(context: Context, rv: RemoteViews, vm: WidgetViewModel) {
        rv.setTextViewText(R.id.streak_value, if (vm.currentStreak == 0) "❄️ ${vm.currentStreak}" else "🔥 ${vm.currentStreak}")
        rv.setTextViewText(R.id.streak_label, "STREAK")
        rv.setTextViewText(R.id.goals_title, "GOALS")
        rv.setTextViewText(R.id.goals_value, "${vm.completedGoals}/${vm.totalGoals}")
        rv.setTextViewText(R.id.rank_value, "${vm.rankIcon} ${vm.rankLabel}")
        rv.setTextViewText(R.id.xp_value, "XP ${vm.xp}  Lvl ${vm.level}")
        rv.setTextViewText(R.id.last_value, "Last ${vm.lastScore}%")
        rv.setTextViewText(R.id.cal_title, "DAILY CHALLENGE")
        rv.setTextViewText(R.id.cal_sub, "${vm.daysDone}/7 days")

        val today = java.time.LocalDate.now()
        for (i in DAY_IDS.indices) {
            val date = today.minusDays((6 - i).toLong())
            val isDone = vm.completedDates.contains(date.toString())
            rv.setTextViewText(DAY_IDS[i], DAY_LABELS[i])
            rv.setInt(DAY_IDS[i], "setBackgroundResource",
                if (isDone) R.drawable.day_done else R.drawable.day_pending)
        }
    }

    /** Paints the streak-only widget. */
    fun paintStreak(context: Context, rv: RemoteViews, vm: WidgetViewModel) {
        rv.setTextViewText(R.id.streak_label, "STREAK")
        rv.setTextViewText(R.id.streak_value, if (vm.currentStreak == 0) "❄️ ${vm.currentStreak}" else "🔥 ${vm.currentStreak}")
    }

    /** Paints the challenge-only widget. */
    fun paintChallenge(context: Context, rv: RemoteViews, vm: WidgetViewModel) {
        rv.setTextViewText(R.id.cal_title, "DAILY CHALLENGE")
        rv.setTextViewText(R.id.cal_sub, "${vm.daysDone}/7 days")

        val today = java.time.LocalDate.now()
        for (i in DAY_IDS.indices) {
            val date = today.minusDays((6 - i).toLong())
            val isDone = vm.completedDates.contains(date.toString())
            rv.setTextViewText(DAY_IDS[i], DAY_LABELS[i])
            rv.setInt(DAY_IDS[i], "setBackgroundResource",
                if (isDone) R.drawable.day_done else R.drawable.day_pending)
        }
    }

    /** Paints the goals-only widget. */
    fun paintGoals(context: Context, rv: RemoteViews, vm: WidgetViewModel) {
        rv.setTextViewText(R.id.goals_title, "GOALS")
        rv.setTextViewText(R.id.goals_value, "${vm.completedGoals}/${vm.totalGoals}")
    }

    /** Paints the XP-only widget. */
    fun paintXp(context: Context, rv: RemoteViews, vm: WidgetViewModel) {
        rv.setTextViewText(R.id.rank_value, "${vm.rankIcon} ${vm.rankLabel}")
        rv.setTextViewText(R.id.xp_value, "XP ${vm.xp}  Lvl ${vm.level}")
        rv.setTextViewText(R.id.last_value, "Last ${vm.lastScore}%")
    }
}
