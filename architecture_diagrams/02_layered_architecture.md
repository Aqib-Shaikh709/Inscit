# 02 — Layered Architecture (Data → Domain → UI)

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│  ANDROID OS / HARDWARE                                                                 │
│  Bluetooth ◄── Nearby ◄── Wi-Fi   |   SharedPreferences   |   WorkManager   |   TTS   │
│  (play-services-nearby)                              (inscit_prefs)   (WorkRequest)  │
└──────────────────────────────────────────────────────────────────────────────────────┘
                                  ▲
                                  │ Context / Permissions / FileProvider
                                  ▼
┌──────────────────────────────────────────────────────────────────────────────────────┐
│  INFRASTRUCTURE / DATA LAYER                                                         │
│                                                                                      │
│  ┌──────────────────┐  ┌──────────────────────┐  ┌───────────────────────────────┐   │
│  │  PrefsWriteLock  │  │   UserRepository     │  │  UserDocument (kotlinx.serial)│   │
│  │  (ReentrantLock) │  │  (interface + Impl)  │  │  ├─ UserProfile               │   │
│  │  serializes every│  │  get/save/update*    │  │  ├─ UserStats (xp, streak..)   │  │
│  │  RMW on inscit_  │  │  observeDocument()   │  │  ├─ QuizProgress               │  │
│  │  prefs to prevent│  │  StateFlow + lock    │  │  ├─ UserSettings (lang/theme)  │  │
│  │  lost updates    │  │  └─ widgets refreshed│  │  ├─ userNotes (Map)            │  │
│  └────────┬─────────┘  └──────────┬───────────┘  │  ├─ dailyChallengeStatus       │  │
│           │ saveDocument/load    │               │  ├─ goals (List<UserGoal>)     │  │
│           │ functions in         │               │  └─ dailyXp (Map<date,Int>)   │   │
│           │ MainActivity.kt      │               └───────────────────────────────┘   │
│           │  (saveUserDocument,  │                              ▲                    │
│           │   loadUserDocument)  │                              │ JSON / legacy pipe│
│           └──────────┬───────────┘                              │ (UserDocumentSaver)│
│                      │                                          │                  │
│                      └──────────────────────┐                   │                  │
│                                             ▼                   ▼                  │
│                                  ┌─────────────────────────────────────────┐       │
│                                  │  assets/questions.json   (4-option MCQ)   │     │
│                                  │  file_paths.xml, backup_rules.xml           │   │
│                                  │  exports/InscitExports (public backup txt)  │   │
│                                  └─────────────────────────────────────────┘       |
└────────────────────────────────────────────────────────────────────────────────────┘
                                  ▲
                                  │ domain calls
                                  ▼
┌──────────────────────────────────────────────────────────────────────────────────────┐
│  DOMAIN / BUSINESS LOGIC (plain Kotlin objects, no Android deps where possible)      |
│                                                                                      │
│  ┌─────────────┐ ┌──────────────┐ ┌─────────────┐ ┌──────────────┐ ┌─────────────┐   │
│  │  QuizEngine │  │   XpManager│  │ StreakManager│ │  GoalManager│  │ StreakTracker│ │
│  │  (banks:    │  │  calculateLevel│  │ updateStreak│  │ applyQuizResult│ │ checkAndReset│ │
│  │   EN/HI +   │  │  Rank enum   │  │ (>=80 extend│  │ (goals + xp)   │ │ IfMissed   │ │
│  │   assets)   │  │              │  │  >=60 maint │  │             │ │ recordQuiz   ││
│  │  analytics, │  │              │  │  <60 break) │  │ GoalScheduler│ │             ││
│  │  daily      │  │              │  │             │  │ (daily alarm)│ │             ││
│  │  rounds 1-3 │  │              │  │ PendingXpBuf│  │             │ │             │ │
│  └──────┬──────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ │
│         │               │                │                │                │         │
│         │               │                │                │                │         │
│  ┌──────┴──────┐ ┌──────┴───────┐ ┌──────┴───────┐ ┌──────┴───────┐ ┌──────┴───────┐ │
│  │Syllabus/*   │ │SprintModels  │ │TransferCrypto│ │UsageAnalytics│ │DailyReportGen│ │
│  │(Physics/Chem│ │VersusModels  │ │(payload hash)│ │Manager       │ │ProgressReport│ │
│  │ Bio, etc.)  │ │VersusResult  │ │NearbyPerms   │ │              │ │VM            │ │
│  └─────────────┘ └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘ │
└──────────────────────────────────────────────────────────────────────────────────────┘
                                  ▲
                                  │ ViewModels consume domain + repository
                                  ▼
┌──────────────────────────────────────────────────────────────────────────────────────┐
│  VIEW MODEL / STATE LAYER                                                            │
│                                                                                      │
│  HomeViewModel ← UserRepository                                                      │
│  QuizViewModel, SprintViewModel (activity-scoped, survives nav),                     │
│  VersusMatchViewModel (activity-scoped), TransferViewModel,                          │
│  ProgressReportViewModel, WidgetViewModel                                            │
└──────────────────────────────────────────────────────────────────────────────────────┘
                                  ▲
                                  │ state hoisted in MainActivity.AppEngine
                                  ▼
┌──────────────────────────────────────────────────────────────────────────────────────┐
│  PRESENTATION / UI LAYER (Jetpack Compose)                                           │
│                                                                                      │
│  MainActivity → AppEngine(tts) → ModalNavigationDrawer + Crossfade(Screen)           │
│                                                                                      │
│  ui/screens:                                                                         │
│  • SPLASH → HOME (drawer + modern home)                                              │
│  • HOME → QUIZ_MODE → { QUIZ | SPRINT_DURATION → SPRINT_ROUND | VERSUS_ROLE → ... }  │
│  • HOME → TOPIC_SELECTION → TOPIC_DETAIL → LAB / NOTES                               │
│  • HOME → PROFILE → { TRANSFER_SEND / TRANSFER_RECEIVE | EXPORTS_LIST }              │
│  • HOME → THEME_CONFIG → { NOTES_FOLDER | NOTES }                                    \
│  • Drawer: RANKINGS, ACHIEVEMENTS, DAILY_QUIZ, PROGRESS_REPORT, REVIEWS, STREAK_DETAILS│
│    GOALS, ABOUT/CONTACT/DONATE, FEEDBACK, NEWS_UPDATES, HELP_CENTER                  /
│                                                                                      │
│  ui/theme: Theme.kt, Colors.kt, Type.kt, Color.kt                                    │
│  ui/*: icons, MicroInteractions, MotionKit, TtsController, ProfileImage, etc.        │
└──────────────────────────────────────────────────────────────────────────────────────┘
                                  ▲
                                  │ AppWidgetProvider callbacks
                                  ▼
┌──────────────────────────────────────────────────────────────────────────────────────┐
│  APP WIDGETS (5 × home-screen widgets, res/xml + res/layout)                         │
│                                                                                      │
│  BaseWidgetProvider                                                                  │
│  ├─ InscitWidgetProvider    (info)      ├─ StreakWidgetProvider   (streak)           │
│  ├─ ChallengeWidgetProvider (challenge) ├─ GoalsWidgetProvider    (goals)            │
│  └─ XpWidgetProvider        (xp)                                                     │
│      WidgetRepository  → WidgetUpdateCoordinator.refreshAll()  → WidgetPainter       │
│      (called on every data change + on boot/update)                                  |
└──────────────────────────────────────────────────────────────────────────────────────┘
                                  ▲
                                  │ WorkManager / AlarmManager
                                  ▼
┌──────────────────────────────────────────────────────────────────────────────────────┐
│  BACKGROUND / NOTIFICATIONS                                                          │
│                                                                                      │
│  NotificationScheduler                                                               │
│  ├─ scheduleInactivityNotification (one-off/worker)                                  │
│  ├─ InactivityWorker (WorkManager)                                                   │
│  └─ cancelAll                                                                        │
│                                                                                      │
│  GoalScheduler.scheduleDailyGoalReminder                                             │
│  NotificationHelper.showNotification (channel + compatibility)                       │
│  GoalScheduler (daily goal reminder alarm)                                           │
└──────────────────────────────────────────────────────────────────────────────────────┘
```