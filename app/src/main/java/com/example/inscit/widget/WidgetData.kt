package com.example.inscit.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.inscit.MainActivity
import com.example.inscit.models.UserDocument
import com.example.inscit.parseUserDocumentJson

object WidgetData {
    fun loadDoc(context: Context): UserDocument? {
        return try {
            val prefs = context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
            val json = prefs.getString("user_data_json", null) ?: return null
            parseUserDocumentJson(json)
        } catch (_: Exception) {
            null
        }
    }

    fun mainIntent(context: Context): PendingIntent {
        val i = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(context, 2001, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun sectionIntent(context: Context, section: String): PendingIntent {
        val i = Intent(context, MainActivity::class.java).apply {
            action = "com.example.inscit.WIDGET_SECTION"
            putExtra("widget_section", section)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(context, section.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
