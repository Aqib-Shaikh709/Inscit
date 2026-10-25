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
import com.example.inscit.xp.Rank
import com.example.inscit.xp.XpManager

class XpWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateSelf(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateSelf(context: Context, mgr: AppWidgetManager, ids: IntArray) {
            val doc = loadDoc(context)
            val stats = doc?.stats
            val rank = Rank.fromXp(stats?.xp ?: 0)
            val level = XpManager.calculateLevel(stats?.xp ?: 0)
            for (id in ids) {
                val rv = RemoteViews(context.packageName, R.layout.widget_xp)
                rv.setTextViewText(R.id.rank_value, "${rank.icon} ${rank.label}")
                rv.setTextViewText(R.id.xp_value, "XP ${stats?.xp ?: 0}  Lvl $level")
                rv.setTextViewText(R.id.last_value, "Last ${(doc?.quizProgress?.lastScore ?: 0f).toInt()}%")

                rv.setOnClickPendingIntent(R.id.widget_root, WidgetData.mainIntent(context))
                rv.setOnClickPendingIntent(R.id.rank_value, WidgetData.sectionIntent(context, "xp"))
                rv.setOnClickPendingIntent(R.id.xp_value, WidgetData.sectionIntent(context, "xp"))
                rv.setOnClickPendingIntent(R.id.last_value, WidgetData.sectionIntent(context, "xp"))

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
