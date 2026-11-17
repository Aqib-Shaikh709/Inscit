# 08 — Background & Notification Flow

```
MainActivity.onCreate
 ├─ NotificationScheduler.scheduleInactivityNotification(this)
 ├─ GoalScheduler.scheduleDailyGoalReminder(this)
 └─ WidgetUpdateCoordinator.refreshAll(this)

Runtime (permission POST_NOTIFICATIONS granted → schedule; denied → cancelAll)

WorkManager:
 ├─ StreakCheckWorker        → StreakTracker.checkAndResetIfMissed (weekly freeze logic)
 └─ InactivityWorker         → fires inactivity notification

Alarm/Job:
 └─ GoalScheduler            → daily goal reminder alarm

Notifications:
 └─ NotificationHelper.showNotification(channel, title, text)
     → "GOAL ACHIEVED!" after completed goals
     → inactivity reminders
```
