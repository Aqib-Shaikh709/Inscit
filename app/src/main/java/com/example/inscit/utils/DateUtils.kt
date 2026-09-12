package com.example.inscit.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val threadLocal = ThreadLocal.withInitial { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    fun today(): String = threadLocal.get()!!.format(Date())
    fun format(date: Date): String = threadLocal.get()!!.format(date)
    fun parse(dateStr: String): Date? = try { threadLocal.get()!!.parse(dateStr) } catch (_: Exception) { null }
    fun yesterday(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_MONTH, -1)
        return format(cal.time)
    }
}
