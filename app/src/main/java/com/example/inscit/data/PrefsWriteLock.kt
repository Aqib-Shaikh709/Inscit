package com.example.inscit.data

import java.util.concurrent.locks.ReentrantLock

/**
 * Process-wide lock serializing every SharedPreferences read-modify-write cycle on
 * `inscit_prefs`.
 *
 * Without this, the 30s persistence loop, UI save callbacks, GoalManager and
 * background Workers interleave RMW on the same prefs file, silently clobbering
 * each other's changes (lost XP, streak, notes, goals). ReentrantLock is used
 * (instead of a coroutine Mutex) so it works from both suspend and non-suspend
 * callers and across UI / Worker threads without making every call site suspend.
 */
object PrefsWriteLock {
    private val lock = ReentrantLock()

    fun <T> withWriteLock(block: () -> T): T {
        lock.lock()
        return try {
            block()
        } finally {
            lock.unlock()
        }
    }
}
