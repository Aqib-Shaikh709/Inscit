package com.example.inscit.widget

import java.time.LocalDate

/**
 * Immutable view model for widget painting.
 *
 * Contains every field required to render any widget type.
 * Produced once per update cycle by WidgetRepository and consumed by WidgetPainter.
 */
data class WidgetViewModel(
    val currentStreak: Int,
    val xp: Int,
    val lastScore: Int,
    val completedGoals: Int,
    val totalGoals: Int,
    val rankIcon: String,
    val rankLabel: String,
    val level: Int,
    val completedDates: Set<String>,
    val daysDone: Int
)
