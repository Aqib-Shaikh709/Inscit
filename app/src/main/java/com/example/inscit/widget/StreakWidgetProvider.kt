package com.example.inscit.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.inscit.MainActivity
import com.example.inscit.R
import com.example.inscit.xp.XpManager

class StreakWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateSelf(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateSelf(context: Context, mgr: AppWidgetManager, ids: IntArray) {
            val doc = WidgetData.loadDoc(context)
            val stats = doc?.stats
            for (id in ids) {
                val rv = RemoteViews(context.packageName, R.layout.widget_streak)
                rv.setTextViewText(R.id.streak_value, "🔥 ${stats?.currentStreak ?: 0}")
                rv.setTextViewText(R.id.streak_label, "STREAK")

                rv.setOnClickPendingIntent(R.id.widget_root, WidgetData.mainIntent(context))
                rv.setOnClickPendingIntent(R.id.streak_value, WidgetData.sectionIntent(context, "streak"))
                rv.setOnClickPendingIntent(R.id.streak_label, WidgetData.sectionIntent(context, "streak"))

                mgr.updateAppWidget(id, rv)
            }
        }
    }
}
