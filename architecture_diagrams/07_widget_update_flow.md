# 07 — Widget Update Flow

```
saveDocument() / 30s persistence loop (dataChanged)
        │
        ▼
WidgetUpdateCoordinator.refreshAll(context)
        │
        ├─ AppWidgetManager.getInstance(context)
        │   ├─ notifyAppWidgetViewDataChanged / updateAppWidget for each:
        │   │   ├─ InscitWidgetProvider   (widget_info.xml)
        │   │   ├─ StreakWidgetProvider   (widget_streak_info.xml)
        │   │   ├─ ChallengeWidgetProvider(widget_challenge_info.xml)
        │   │   ├─ GoalsWidgetProvider    (widget_goals_info.xml)
        │   │   └─ XpWidgetProvider       (widget_xp_info.xml)
        │   │
        │   ▼
        │   WidgetRepository reads UserRepository.getDocument()
        │   ▼
        │   RemoteViews + WidgetPainter → home screen
        │
        └─ also triggered on: boot, APPWIDGET_UPDATE broadcast
```
