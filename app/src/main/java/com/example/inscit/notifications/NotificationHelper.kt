package com.example.inscit.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.inscit.MainActivity
import com.example.inscit.R

object NotificationHelper {
    private const val CHANNEL_ID = "inactivity_channel"
    private const val CHANNEL_NAME = "Daily Motivation"
    private const val CHANNEL_DESC = "Notifications to keep you motivated and learning!"

    // Single gate for the Settings toggle (default ON). Reads JSON primary, tolerates legacy.
    fun isEnabled(context: Context): Boolean {
        return try {
            val prefs = context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
            val json = prefs.getString("user_data_json", null)
            if (json != null) {
                com.example.inscit.parseUserDocumentJson(json)?.settings?.notificationsEnabled ?: true
            } else true
        } catch (_: Exception) { true }
    }

    fun userLanguage(context: Context): com.example.inscit.models.Lang {
        return try {
            val prefs = context.getSharedPreferences("inscit_prefs", Context.MODE_PRIVATE)
            val json = prefs.getString("user_data_json", null)
            if (json != null) {
                com.example.inscit.parseUserDocumentJson(json)?.settings?.language
                    ?: com.example.inscit.models.Lang.EN
            } else com.example.inscit.models.Lang.EN
        } catch (_: Exception) { com.example.inscit.models.Lang.EN }
    }

    fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = CHANNEL_DESC
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Use app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notifId = (System.currentTimeMillis() and 0xFFFFFF).toInt()
        notificationManager.notify(notifId, builder.build())
    }
}
