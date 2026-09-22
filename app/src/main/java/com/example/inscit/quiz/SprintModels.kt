package com.example.inscit.quiz

import androidx.compose.runtime.Immutable

// Bullet-chess style durations from the flowchart diamond.
enum class SprintDuration(val seconds: Int, val labelEn: String, val labelHi: String) {
    SEC_10(10, "10 SEC", "10 सेकंड"),
    SEC_30(30, "30 SEC", "30 सेकंड"),
    MIN_1(60, "1 MIN", "1 मिनट")
}

sealed class SprintUiState {
    object Idle : SprintUiState()
    data class Running(
        val currentQuestion: ScienceQuestion,
        val answered: Int,
        val correct: Int,
        val timeLeftMs: Long,
        val totalMs: Long,
        val selectedOptionId: Int? = null,
        val isTransitioning: Boolean = false
    ) : SprintUiState()

    data class Finished(val analytics: SprintAnalytics) : SprintUiState()
}

@Immutable
data class SprintAnalytics(
    val attempted: Int,
    val correct: Int,
    val wrong: Int,
    // Reuses the normal radar so the result screen keeps its pentagonal graph.
    val overallScore: Int,
    val radarData: List<DomainScore>,
    val strengthsEn: List<String>,
    val strengthsHi: List<String>,
    val weaknessesEn: List<String>,
    val weaknessesHi: List<String>,
    val explanations: List<Pair<String, String>>
)
