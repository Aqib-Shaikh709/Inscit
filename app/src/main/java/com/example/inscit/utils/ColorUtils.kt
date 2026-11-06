package com.example.inscit.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Converts a [Color] to a hex string like "#FF5733" (RGB only, alpha stripped).
 */
fun colorToHex(color: Color): String {
    val argb = color.toArgb().toInt() and 0xFFFFFF
    return "#${argb.toString(16).padStart(6, '0').uppercase()}"
}

/**
 * Parses a hex string like "#FF5733" or "FF5733" into a [Color].
 * Returns null if the string is not a valid 6-digit hex color.
 */
fun hexToColor(hex: String): Color? {
    return try {
        val clean = hex.removePrefix("#").trim()
        if (clean.length != 6) return null
        clean.toInt(16) // validate all characters are hex digits
        val androidColor = android.graphics.Color.parseColor("#$clean")
        Color(androidColor)
    } catch (_: NumberFormatException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}
