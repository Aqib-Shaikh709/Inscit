package com.example.inscit

import com.example.inscit.models.Lang
import com.example.inscit.quiz.QuizEngine
import com.example.inscit.quiz.ScienceDomain
import org.junit.Assert.*
import org.junit.Test

// Replays the quiz-finish path: bank load -> answer all -> calculateAnalytics -> weak mapping.
class QuizEngineTest {
    private val engine = QuizEngine()

    @Test
    fun englishBank_hasTenQuestionsWithSingleCorrect() {
        val qs = engine.getQuestions(Lang.EN, 10, null)
        assertEquals(10, qs.size)
        qs.forEach { q ->
            assertTrue("need >=2 options: ${q.id}", q.options.size >= 2)
            assertEquals("exactly 1 correct: ${q.id}", 1, q.options.count { it.isCorrect })
        }
    }

    @Test
    fun hindiBank_loads() {
        val qs = engine.getQuestions(Lang.HI, 10, null)
        assertEquals(10, qs.size)
    }

    @Test
    fun analytics_allCorrect_gives100_noWeaknesses() {
        val qs = engine.getQuestions(Lang.EN, 10, null)
        val answers = qs.associate { it.id to it.options.first { o -> o.isCorrect } }
        val a = engine.calculateAnalytics(qs, answers)
        assertEquals(100, a.overallScore)
        assertTrue(a.weaknessesEn.isEmpty())
    }

    @Test
    fun analytics_allWrong_gives0_withWeaknesses() {
        val qs = engine.getQuestions(Lang.EN, 10, null)
        val answers = qs.associate { q ->
            q.id to (q.options.firstOrNull { !it.isCorrect } ?: q.options.first())
        }
        val a = engine.calculateAnalytics(qs, answers)
        assertEquals(0, a.overallScore)
        assertTrue(a.weaknessesEn.isNotEmpty())
        // weak names must resolve back to domains (Practice Weakness mapping)
        val names = a.weaknessesEn + a.weaknessesHi
        val resolved = ScienceDomain.entries.filter { d -> d.displayNameEn in names || d.displayNameHi in names }
        assertTrue(resolved.isNotEmpty())
    }

    @Test
    fun analytics_weakDomains_weighted() {
        val weak = setOf(ScienceDomain.ASTRONOMY)
        // over many draws the weak domain must appear (2x weight on small bank)
        val seen = (1..20).flatMap { engine.getQuestions(Lang.EN, 10, null, weak) }
        assertTrue(seen.any { it.domain == ScienceDomain.ASTRONOMY })
    }

    @Test
    fun difficultyFilter_fallsBackWhenEmpty() {
        val qs = engine.getQuestions(Lang.EN, 10, "ADVANCED")
        assertEquals(10, qs.size)
    }

    @Test
    fun correctAnswer_positionVariesAcrossQuizzes() {
        // Regression: banks store the correct option first - getQuestions must shuffle
        // so users can't game the quiz by always tapping option 1.
        val positions = (1..20).map {
            engine.getQuestions(Lang.EN, 10, null).first()
                .let { q -> q.options.indexOfFirst { it.isCorrect } }
        }
        assertTrue("correct answer stuck at one position", positions.toSet().size > 1)
    }

    @Test
    fun dailyRounds_optionsShuffled() {
        val seen = (1..20).flatMap { engine.getDailyRoundQuestions(1, Lang.EN) }
            .filter { it.id == "r1_1" }
            .map { q -> q.options.indexOfFirst { it.isCorrect } }
            .toSet()
        assertTrue("daily correct answer stuck at one position", seen.size > 1)
    }
}
