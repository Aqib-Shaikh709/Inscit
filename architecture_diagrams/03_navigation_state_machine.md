# 03 — Navigation State Machine

```
                        onCreate
                           │
                           ▼
                    ┌──────────┐
                    │  SPLASH  │──(tts init, goal/notification scheduling,
                    │ (splash) │   widget refresh, StreakTracker.check)──┐
                    └────┬─────┘                                         │
                         │ currentScreen = HOME                          │
                         ▼                                             │
                    ┌──────────┐         ┌─────────────────────┐       │
                    │   HOME   │────────▶│   Drawer (modal)    │       │
                    │(ModernHome│         │  (300.dp width)     │       │
                    │  cards)  │         │  ← all screens here │       │
                    └────┬─────┘         └─────────────────────┘       │
                         │                                              │
      ┌──────────────────┼────────────────────┐                         │
      │                  │                    │                         │
      ▼                  ▼                    ▼                         │
┌──────────┐     ┌───────────┐       ┌──────────────┐                  │
│  LAB     │     │  PROFILE  │       │  QUIZ_MODE   │                  │
│(branch)  │     │(local/export│       │  (Normal/Sprint│                  │
│ → NOTES  │     │  /transfer) │       │   /Versus)   │                  │
└────┬─────┘     └────┬──────┘       └──────┬───────┘                  │
     │                │                     │                           │
     │                ├─ TRANSFER_SEND ◄───┼─► NearbyTransferManager   │
     │                ├─ TRANSFER_RECEIVE ─┼─► (Google Play Nearby P2P)│
     │                └─ EXPORTS_LIST      │                           │
     │                                     ▼                           │
     │                            ┌──────────────┐                      │
     │                            │  VERSUS_ROLE │◄─ host/join choice   │
     │                            └──────┬───────┘                      │
     │                   ┌──────────┴──────────┐                        │
     │                   ▼                     ▼                        │
     │             ┌──────────┐        ┌──────────┐                    │
     │             │ VERSUS_  │        │ VERSUS_  │                    │
     │             │   HOST   │        │   JOIN   │                    │
     │             └────┬─────┘        └────┬─────┘                    │
     │                  │                  │                            │
     │                  └──────────────────┘                            │
     │                            │                                      │
     │                            ▼                                      │
     │                      ┌──────────┐                                 │
     │                      │ VERSUS_  │                                 │
     │                      │   ROUND  │◄─── VersusManager (send score) ──┘
     │                      └────┬─────┘      (P2P, both scores → result)
     │                           │
     │                    ┌──────┴──────┐
     │                    ▼             ▼
     │              ┌────────┐  ┌────────────┐
     │              │ RESULT │──►│ XP/streak/ │
     │              │(VERSUS_│   │ goals applied│
     │              │ RESULT)│   │ → QUIZ_MODE  │
     │              └────────┘  └────────────┘
     │
     ▼
┌──────────┐     ┌──────────────┐     ┌──────────────┐
│ TOPIC_SEL│     │ TOPIC_DETAIL │     │   QUIZ       │
│(Branch)  │     │(selectedTopic│     │(ScienceQuiz) │
└────┬─────┘     │  + note)     │     └──────┬───────┘
     │          └──────┬────────┘           │
     │                 │                    ▼
     │                 ▼            ┌──────────────┐
     │             ┌──────────┐     │ onFinish →   │
     │             │   LAB    │     │ StreakManager│
     │             │(tts sim) │     │ XpManager    │
     │             └────┬─────┘     │ GoalManager  │
     │                  │           └──────┬───────┘
     │                  ▼                  │
     │             ┌──────────┐            ▼
     │             │  NOTES   │        ┌──────────┐
     │             │(per branch│        │   HOME   │
     │             │  note)   │        └──────────┘
     │             └──────────┘           ▲  (30s debounced save)
     │                  │                 │
     └──────────────────┴─────────────────┘
          ThemeConfig / DailyQuiz / ProgressReport / Goals / StreakDetails /
          Rankings / Achievements / Reviews / Feedback / About / Contact / Donate
          → all navigate back to HOME via onBack
```
