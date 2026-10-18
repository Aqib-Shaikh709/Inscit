package com.example.inscit.xp

class PendingXpBuffer {
    private var _pendingXp = 0
    val pendingXp: Int get() = _pendingXp

    fun add(amount: Int) {
        _pendingXp += amount
    }

    fun clear() {
        _pendingXp = 0
    }
}
