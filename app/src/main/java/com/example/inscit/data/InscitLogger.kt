package com.example.inscit.data

import android.util.Log

/**
 * Centralized logger so no exception is swallowed silently.
 *
 * Use this in persistence / data-mutation catch blocks instead of
 * `catch (_: Exception) {}` so failures are visible in logcat during
 * development and can be wired to crash reporting later.
 */
object InscitLogger {
    private const val TAG = "Inscit"

    fun w(msg: String, throwable: Throwable? = null) = Log.w(TAG, msg, throwable)
    fun e(msg: String, throwable: Throwable? = null) = Log.e(TAG, msg, throwable)
    fun i(msg: String, throwable: Throwable? = null) = Log.i(TAG, msg, throwable)
}
