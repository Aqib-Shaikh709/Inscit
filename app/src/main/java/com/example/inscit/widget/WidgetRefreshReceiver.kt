package com.example.inscit.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.appwidget.AppWidgetManager

class WidgetRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val mgr = AppWidgetManager.getInstance(context)
        WidgetRefreshManager.maybeRepaintAll(context)
    }
}
