package com.example.inscit.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import com.example.inscit.R

/**
 * Orchestrates widget updates.
 *
 * Design principles (fixes junior-dev issues):
 *   • Update on data change only — no polling AlarmManager, no battery drain
 *   • Load data once, paint all widget types once — no O(n²) redundant work
 *   • Each provider paints only its own widget type — no cross-painting
 *   • No WidgetRefreshReceiver feedback loop — removed entirely
 */
object WidgetUpdateCoordinator {

    /**
     * Refreshes all widget types.
     * Call this whenever user data changes (quiz end, goal complete, etc.).
     */
    fun refreshAll(context: Context) {
        val vm = WidgetRepository.loadViewModel(context) ?: return
        val mgr = AppWidgetManager.getInstance(context)

        paintAllInOne(context, mgr, vm)
        paintStreak(context, mgr, vm)
        paintChallenge(context, mgr, vm)
        paintGoals(context, mgr, vm)
        paintXp(context, mgr, vm)
    }

    /**
     * Refreshes only the widgets for a specific provider.
     * Call this from AppWidgetProvider.onUpdate() so only that provider's widgets repaint.
     */
    fun refreshProvider(context: Context, componentName: ComponentName) {
        val vm = WidgetRepository.loadViewModel(context) ?: return
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(componentName)
        if (ids.isEmpty()) return

        when (componentName) {
            ComponentName(context, InscitWidgetProvider::class.java) -> paintAllInOneIds(context, mgr, vm, ids)
            ComponentName(context, StreakWidgetProvider::class.java) -> paintStreakIds(context, mgr, vm, ids)
            ComponentName(context, ChallengeWidgetProvider::class.java) -> paintChallengeIds(context, mgr, vm, ids)
            ComponentName(context, GoalsWidgetProvider::class.java) -> paintGoalsIds(context, mgr, vm, ids)
            ComponentName(context, XpWidgetProvider::class.java) -> paintXpIds(context, mgr, vm, ids)
            else -> { /* unknown provider — ignore */ }
        }
    }

    /* ---------- All-in-one widget ---------- */

    private fun paintAllInOne(context: Context, mgr: AppWidgetManager, vm: WidgetViewModel) {
        val cn = ComponentName(context, InscitWidgetProvider::class.java)
        val ids = mgr.getAppWidgetIds(cn)
        if (ids.isEmpty()) return
        paintAllInOneIds(context, mgr, vm, ids)
    }

    private fun paintAllInOneIds(
        context: Context,
        mgr: AppWidgetManager,
        vm: WidgetViewModel,
        ids: IntArray
    ) {
        for (id in ids) {
            val rv = RemoteViews(context.packageName, R.layout.widget_inscit)
            WidgetPainter.paintAllInOne(context, rv, vm)
            WidgetPainter.setMainIntent(context, rv, R.id.widget_root, WidgetPainter.REQUEST_MAIN)
            WidgetPainter.setSectionIntent(context, rv, R.id.card_streak, "streak")
            WidgetPainter.setSectionIntent(context, rv, R.id.card_calendar, "challenge")
            WidgetPainter.setSectionIntent(context, rv, R.id.card_goals, "goals")
            WidgetPainter.setSectionIntent(context, rv, R.id.card_rank, "xp")
            mgr.updateAppWidget(id, rv)
        }
    }

    /* ---------- Streak widget ---------- */

    private fun paintStreak(context: Context, mgr: AppWidgetManager, vm: WidgetViewModel) {
        val cn = ComponentName(context, StreakWidgetProvider::class.java)
        val ids = mgr.getAppWidgetIds(cn)
        if (ids.isEmpty()) return
        paintStreakIds(context, mgr, vm, ids)
    }

    private fun paintStreakIds(
        context: Context,
        mgr: AppWidgetManager,
        vm: WidgetViewModel,
        ids: IntArray
    ) {
        for (id in ids) {
            val rv = RemoteViews(context.packageName, R.layout.widget_streak)
            WidgetPainter.paintStreak(context, rv, vm)
            WidgetPainter.setMainIntent(context, rv, R.id.widget_root, WidgetPainter.REQUEST_MAIN_STREAK)
            WidgetPainter.setSectionIntent(context, rv, R.id.streak_value, "streak")
            WidgetPainter.setSectionIntent(context, rv, R.id.streak_label, "streak")
            mgr.updateAppWidget(id, rv)
        }
    }

    /* ---------- Challenge widget ---------- */

    private fun paintChallenge(context: Context, mgr: AppWidgetManager, vm: WidgetViewModel) {
        val cn = ComponentName(context, ChallengeWidgetProvider::class.java)
        val ids = mgr.getAppWidgetIds(cn)
        if (ids.isEmpty()) return
        paintChallengeIds(context, mgr, vm, ids)
    }

    private fun paintChallengeIds(
        context: Context,
        mgr: AppWidgetManager,
        vm: WidgetViewModel,
        ids: IntArray
    ) {
        val dayIds = intArrayOf(R.id.cd0, R.id.cd1, R.id.cd2, R.id.cd3, R.id.cd4, R.id.cd5, R.id.cd6)
        for (id in ids) {
            val rv = RemoteViews(context.packageName, R.layout.widget_challenge)
            WidgetPainter.paintChallenge(context, rv, vm)
            WidgetPainter.setMainIntent(context, rv, R.id.widget_root, WidgetPainter.REQUEST_MAIN_CHALLENGE)
            WidgetPainter.setSectionIntent(context, rv, R.id.cal_title, "challenge")
            WidgetPainter.setSectionIntent(context, rv, R.id.cal_sub, "challenge")
            for (dotId in dayIds) {
                WidgetPainter.setSectionIntent(context, rv, dotId, "challenge")
            }
            mgr.updateAppWidget(id, rv)
        }
    }

    /* ---------- Goals widget ---------- */

    private fun paintGoals(context: Context, mgr: AppWidgetManager, vm: WidgetViewModel) {
        val cn = ComponentName(context, GoalsWidgetProvider::class.java)
        val ids = mgr.getAppWidgetIds(cn)
        if (ids.isEmpty()) return
        paintGoalsIds(context, mgr, vm, ids)
    }

    private fun paintGoalsIds(
        context: Context,
        mgr: AppWidgetManager,
        vm: WidgetViewModel,
        ids: IntArray
    ) {
        for (id in ids) {
            val rv = RemoteViews(context.packageName, R.layout.widget_goals)
            WidgetPainter.paintGoals(context, rv, vm)
            WidgetPainter.setMainIntent(context, rv, R.id.widget_root, WidgetPainter.REQUEST_MAIN_GOALS)
            WidgetPainter.setSectionIntent(context, rv, R.id.goals_value, "goals")
            WidgetPainter.setSectionIntent(context, rv, R.id.goals_title, "goals")
            mgr.updateAppWidget(id, rv)
        }
    }

    /* ---------- XP widget ---------- */

    private fun paintXp(context: Context, mgr: AppWidgetManager, vm: WidgetViewModel) {
        val cn = ComponentName(context, XpWidgetProvider::class.java)
        val ids = mgr.getAppWidgetIds(cn)
        if (ids.isEmpty()) return
        paintXpIds(context, mgr, vm, ids)
    }

    private fun paintXpIds(
        context: Context,
        mgr: AppWidgetManager,
        vm: WidgetViewModel,
        ids: IntArray
    ) {
        for (id in ids) {
            val rv = RemoteViews(context.packageName, R.layout.widget_xp)
            WidgetPainter.paintXp(context, rv, vm)
            WidgetPainter.setMainIntent(context, rv, R.id.widget_root, WidgetPainter.REQUEST_MAIN_XP)
            WidgetPainter.setSectionIntent(context, rv, R.id.rank_value, "xp")
            WidgetPainter.setSectionIntent(context, rv, R.id.xp_value, "xp")
            WidgetPainter.setSectionIntent(context, rv, R.id.last_value, "xp")
            mgr.updateAppWidget(id, rv)
        }
    }
}
