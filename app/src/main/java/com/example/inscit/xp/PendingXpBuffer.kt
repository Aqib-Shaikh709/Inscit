package com.example.inscit.xp

/**
 * Accumulates per-question XP during a quiz/sprint round.
 *
 * Correct = +10, wrong = -2. The buffer is clamped at 0 so a run of wrong
 * answers can never drive pending XP negative (which previously showed as
 * negative XP in the UI and got clamped to 0 at award time, erasing earned
 * XP). Award-time clamping [QuizViewModel.getFinalXp / SprintViewModel.getFinalXp
 * / VersusMatchViewModel.getFinalXp] still applies maxOf(0, ...) as a belt
 * and suspenders.
 */
class PendingXpBuffer {
    private var _pendingXp = 0
    val pendingXp: Int get() = _pendingXp

    fun add(amount: Int) {
        _pendingXp = (_pendingXp + amount).coerceAtLeast(0)
    }

    fun clear() {
        _pendingXp = 0
    }
}
