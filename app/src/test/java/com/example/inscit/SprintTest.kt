package com.example.inscit

import com.example.inscit.models.Lang
import com.example.inscit.quiz.QuizEngine
import com.example.inscit.quiz.SprintDuration
import com.example.inscit.quiz.SprintViewModel
import org.junit.Assert.*
import org.junit.Test

class SprintTest {
    private val engine = QuizEngine()

    @Test
    fun durations_matchFlowchart() {
        assertEquals(10, SprintDuration.SEC_10.seconds)
        assertEquals(30, SprintDuration.SEC_30.seconds)
        assertEquals(60, SprintDuration.MIN_1.seconds)
    }

    @Test
    fun hardcodedFallback_coversAllDomains() {
        // No Context on JVM, so this exercises the hardcoded fallback (incl. new Astro/Geo).
        val all = engine.getQuestions(Lang.EN, Int.MAX_VALUE, null)
        assertTrue("fallback bank too small, was ${all.size}", all.size >= 28)
        assertTrue(all.map { it.domain }.toSet().size >= 5)
    }

    @kotlinx.serialization.Serializable
    private data class AssetOption(val id: Int, val text: String, val isCorrect: Boolean)

    @kotlinx.serialization.Serializable
    private data class AssetQuestion(
        val id: String,
        val domain: String,
        val text: String,
        val options: List<AssetOption>,
        val explanation: String,
        val difficulty: String = "BASIC"
    )

    @Test
    fun primeAssetsFile_validAndLarge() {
        // The on-device prime source: src/main/assets/questions.json resolved from module dir.
        val file = java.io.File("src/main/assets/questions.json")
        assertTrue("questions.json missing", file.exists())
        val dtos = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            .decodeFromString<List<AssetQuestion>>(file.readText())
        assertTrue("prime bank must be large, was ${dtos.size}", dtos.size >= 60)
        assertTrue(dtos.map { it.domain }.toSet().size >= 5)
        assertEquals("duplicate ids", dtos.size, dtos.map { it.id }.toSet().size)
        dtos.forEach { q ->
            assertEquals("exactly 1 correct: ${q.id}", 1, q.options.count { it.isCorrect })
            assertTrue("need 4 options: ${q.id}", q.options.size == 4)
        }
    }

    @Test
    fun sprintXp_partialScore_noBonus() {
        val vm = SprintViewModel()
        // 0 in buffer, 50% score, no streak -> 0
        assertEquals(0, vm.getFinalXp(0, 50))
    }
}
