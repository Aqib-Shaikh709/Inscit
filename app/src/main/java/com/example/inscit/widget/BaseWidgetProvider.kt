package com.example.inscit.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context

/**
 * Base AppWidgetProvider that delegates to WidgetUpdateCoordinator.
 *
 * Each concrete provider only declares its class — onUpdate repaints only that provider's
 * widgets, onEnabled triggers a full refresh of all widget types. No polling, no AlarmManager,
 * no WidgetRefreshReceiver feedback loop.
 */
abstract class BaseWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetUpdateCoordinator.refreshProvider(
            context,
            ComponentName(context, this::class.java)
        )
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateCoordinator.refreshAll(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Nothing to clean up — we don't register alarms or receivers.
    }
}
