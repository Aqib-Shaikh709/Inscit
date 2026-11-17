# 06 — Versus — Offline PvP (Google Play Nearby)

```
QUIZ_MODE ──onVersus──▶ VERSUS_ROLE ──host/join──▶
        │                                         │
        │                                         ▼
        │                                   ┌──────────┐ ┌──────────┐
        │                                   │  HOST    │ │  JOIN    │
        │                                   │ VersusHostScreen │ VersusJoinScreen
        │                                   │ (advertise)│ (discover)│
        │                                   └────┬─────┘ └────┬─────┘
        │                                        │            │
        │                                        │ NearbyTransferManager / VersusManager
        │                                        │  (endpoint ID, Bluetooth+WiFi+Nearby)
        │                                        ▼            ▼
        │                                   ┌─────────────────────────┐
        │                                   │   Handshake → Match payload
        │                                   │   (VersusMatchPayload)  │
        │                                   │   VersusMatchViewModel  │
        │                                   │   (activity-scoped)     │
        │                                   └──────────┬──────────────┘
        │                                              │
        │                                              ▼
        │                                        ┌──────────┐
        │                                        │ VERSUS_  │
        │                                        │   ROUND  │─── both play same questions
        │                                        │(Sprint-like│─► sendScore(ep, buildScorePayload)
        │                                         round)    │      (VersusScorePayload, hashed)
        │                                        └────┬─────┘
        │                                             │
        │                    ┌────────────────────────┘
        │                    │  both scores in (versusMyDone && peerScore)
        │                    ▼
        │              ┌──────────┐
        │              │ winner   │──► versusWinnerFor(attempted, correct, isHost, peer)
        │              │ + XP     │      VersusResult: HOST_WINS / GUEST_WINS / DRAW
        │              │ reward   │──► StreakManager + XpManager + GoalManager
        │              └────┬─────┘
        │                   │
        │                   ▼
        │              ┌──────────┐
        │              │ VERSUS_  │── onDone → disconnect + reset → QUIZ_MODE
        │              │  RESULT  │
        │              └──────────┘
        │
        └─ (Back / Exit at any stage → versusManager.disconnect() → VERSUS_ROLE)
```
