# 05 — Quiz / Sprint Flow

```
HOME ──onQuiz──▶ QUIZ_MODE ──onNormal──▶ QUIZ (ScienceQuizScreen)
                                    │
                                    │ QuizEngine.getQuestions(lang, count=10,
                                    │   weakDomains=weaknesses)           │
                                    │   → assets/questions.json (EN)      │
                                    │   → hardcoded EN/HI bank (fallback) │
                                    ▼
                              user answers (Map<id, QuizOption>)
                                    │
                                    ▼
                              QuizEngine.calculateAnalytics()
                              → overallScore, strengths, weaknesses,
                                scienceType, radarData
                                    │
                                    ▼
                    StreakManager.updateStreak(stats, score)   (≥80 extend, ≥60 maintain, <60 break)
                    XpManager.calculateLevel(newXp)            (level = xp/100 + 1)
                    GoalManager.applyQuizResult(doc, xp, score) (goal progress + completed)
                    StreakTracker.recordQuiz(context, score)
                    NotificationHelper("GOAL ACHIEVED!") per completed goal
                    NotificationScheduler / GoalScheduler re-scheduled
                                    │
                                    ▼
                              userDocument updated → saved (30s loop) → back to HOME

───────────────────────────────────────────────────────────────────────────────
SPRINT path:
QUIZ_MODE ──onSprint──▶ SPRINT_DURATION ──pick──▶ SPRINT_ROUND (SprintQuizScreen)
                                                │  (activity-scoped SprintViewModel
                                                │   timer — survives nav)
                                                ▼
                                          onFinish(xpEarned, analytics)
                                          → same reward pipeline as QUIZ
                                          → sprintVm.cancel() → HOME
```
