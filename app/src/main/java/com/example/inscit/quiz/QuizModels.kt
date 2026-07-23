package com.example.inscit.quiz

import androidx.compose.runtime.Immutable
import com.example.inscit.models.Lang

enum class ScienceDomain(val displayNameEn: String, val displayNameHi: String) {
    PHYSICS("Physics", "भौतिकी"),
    CHEMISTRY("Chemistry", "रसायन विज्ञान"),
    BIOLOGY("Biology", "जीव विज्ञान"),
    ASTRONOMY("Astronomy", "खगोल विज्ञान"),
    GEOLOGY("Geology", "भूविज्ञान")
}

@Immutable
data class QuizOption(
    val id: Int,
    val text: String,
    val isCorrect: Boolean
)

@Immutable
data class ScienceQuestion(
    val id: String,
    val domain: ScienceDomain,
    val text: String,
    val options: List<QuizOption>,
    val weight: Float = 1.0f,
    val explanation: String,
    val difficulty: String = "BASIC"
)

@Immutable
data class DomainScore(
    val domain: ScienceDomain,
    val score: Float,
    val totalQuestions: Int
)

@Immutable
data class ScienceAnalytics(
    val overallScore: Int,
    val scienceTypeEn: String,
    val scienceTypeHi: String,
    val radarData: List<DomainScore>,
    val strengthsEn: List<String>,
    val strengthsHi: List<String>,
    val weaknessesEn: List<String>,
    val weaknessesHi: List<String>,
    val averageEn: List<String>,
    val averageHi: List<String>,
    val explanations: List<Pair<String, String>>
)
