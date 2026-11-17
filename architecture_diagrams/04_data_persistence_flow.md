# 04 — Data Persistence Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  LaunchedEffect(Unit) in AppEngine — runs forever while activity is active   │
│                                                                             │
│   delay(1000ms) ───► usageSeconds++ (per minute → totalUsageTime += 60000) │
│                       │                                                      │
│                       ├─ if (userDocument != lastSavedDocument)              │
│                       │      dataChanged = true                              │
│                       │                                                      │
│                       └─ if (changed && now - lastSave >= 30s)               │
│                              saveUserDocument(context, userDocument)         │
│                                      │                                       │
│                                      ▼                                       │
│                    ┌─────────────────────────────┐                           │
│                    │   PrefsWriteLock.withWriteLock│                          │
│                    │   (ReentrantLock — process-wide)│                        │
│                    └──────────────┬───────────────┘                           │
│                                   │                                           │
│                                   ▼                                           │
│                    SharedPreferences.edit()                                   │
│                    ├─ putString("user_data_json", serializeUserDocumentJson)  │
│                    ├─ putString("user_data", serializeUserDocument  (legacy)  │
│                    └─ apply()                                                 │
│                                   │                                           │
│                                   ▼                                           │
│                    openFileOutput("inscit_backup.dat")  (local backup file)   │
│                                   │                                           │
│                                   ▼                                           │
│                    WidgetUpdateCoordinator.refreshAll(context)   ← only on    │
│                    (5 widgets repainted via WidgetPainter)   dataChanged     │
│                                                                             │
│  LOAD (process death / start):                                              │
│  loadUserDocument → JSON (user_data_json)                                   │
│            → backup.dat (JSON or legacy pipe)                               │
│            → legacy pipe "user_data" (UserDocumentSaver)                    │
│            → fallback UserDocument("Core Explorer")                         │
└─────────────────────────────────────────────────────────────────────────────┘

NOTE: HomeViewModel also wraps UserRepository (separate path) — but most UI
mutates `userDocument` directly in MainActivity and calls saveUserDocument(),
so the Repository is largely a parallel API used by widgets/tests.
```
