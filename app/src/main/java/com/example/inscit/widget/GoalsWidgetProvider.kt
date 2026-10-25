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

class GoalsWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateSelf(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateSelf(context: Context, mgr: AppWidgetManager, ids: IntArray) {
            val doc = loadDoc(context)
            val completed = doc?.goals?.count { it.isCompleted } ?: 0
            val total = doc?.goals?.size ?: 0
            for (id in ids) {
                val rv = RemoteViews(context.packageName, R.layout.widget_goals)
                rv.setTextViewText(R.id.goals_title, "GOALS")
                rv.setTextViewText(R.id.goals_value, "$completed/$total")

                rv.setOnClickPendingIntent(R.id.widget_root, WidgetData.mainIntent(context))
                rv.setOnClickPendingIntent(R.id.goals_value, WidgetData.sectionIntent(context, "goals"))
                rv.setOnClickPendingIntent(R.id.goals_title, WidgetData.sectionIntent(context, "goals"))

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
