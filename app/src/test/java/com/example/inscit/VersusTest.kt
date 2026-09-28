package com.example.inscit

import com.example.inscit.quiz.QuizOption
import com.example.inscit.quiz.ScienceDomain
import com.example.inscit.quiz.ScienceQuestion
import com.example.inscit.transfer.VersusCrypto
import com.example.inscit.transfer.VersusMatchPayload
import com.example.inscit.transfer.VersusResult
import com.example.inscit.transfer.VersusScorePayload
import com.example.inscit.transfer.VersusWire
import com.example.inscit.transfer.buildMatchPayload
import com.example.inscit.transfer.buildScorePayload
import com.example.inscit.transfer.decideVersusWinner
import com.example.inscit.transfer.toDto
import com.example.inscit.transfer.toQuestion
import org.junit.Assert.*
import org.junit.Test

class VersusTest {

    private fun sampleBank(): List<ScienceQuestion> = listOf(
        ScienceQuestion(
            id = "q1", domain = ScienceDomain.PHYSICS, text = "Q1?",
            options = listOf(QuizOption(1, "A", true), QuizOption(2, "B", false)),
            explanation = "E1"
        ),
        ScienceQuestion(
            id = "q2", domain = ScienceDomain.CHEMISTRY, text = "Q2?",
            options = listOf(QuizOption(1, "C", false), QuizOption(2, "D", true)),
            explanation = "E2"
        )
    )

    @Test
    fun sha256_isDeterministic_hex64() {
        val a = VersusCrypto.sha256Hex("match|5|4|abc")
        val b = VersusCrypto.sha256Hex("match|5|4|abc")
        assertEquals(a, b)
        assertEquals(64, a.length)
        assertNotEquals(a, VersusCrypto.sha256Hex("match|5|3|abc"))
    }

    @Test
    fun questionDto_roundTrip_preservesCorrectAnswer() {
        val q = sampleBank().first()
        val restored = q.toDto().toQuestion()
        assertEquals(q.id, restored.id)
        assertEquals(q.domain, restored.domain)
        assertEquals(1, restored.options.count { it.isCorrect })
        assertEquals(q.options.first { it.isCorrect }.text, restored.options.first { it.isCorrect }.text)
    }

    @Test
    fun matchPayload_hashCoversQuestionOrder() {
        val bank = sampleBank()
        val m1 = buildMatchPayload("A", 30, bank, matchId = "m1")
        val m2 = buildMatchPayload("A", 30, bank.reversed(), matchId = "m1")
        assertNotEquals(m1.questionsHash, m2.questionsHash)
        assertEquals(2, m1.questions.size)
        assertEquals(30, m1.durationSeconds)
    }

    @Test
    fun score_verify_acceptsHonest_rejectsTampered() {
        val match = buildMatchPayload("A", 30, sampleBank(), matchId = "m9")
        val honest = buildScorePayload(match, "B", isHost = false, attempted = 2, correct = 1)
        assertTrue(VersusCrypto.verifyScore(honest, match.matchId, match.questionsHash))
        // Tamper: bump correct without fixing hash/score.
        val tampered = honest.copy(correct = 2)
        assertFalse(VersusCrypto.verifyScore(tampered, match.matchId, match.questionsHash))
        // Wrong match id.
        assertFalse(VersusCrypto.verifyScore(honest, "other", match.questionsHash))
    }

    @Test
    fun envelope_packUnpack_roundTrip() {
        val match = buildMatchPayload("A", 10, sampleBank(), matchId = "mx")
        val bytes = VersusWire.pack(VersusWire.TYPE_MATCH, VersusWire.encode(match))
        val env = VersusWire.unpack(bytes)
        assertNotNull(env)
        assertEquals(VersusWire.TYPE_MATCH, env!!.type)
        val decoded = VersusWire.decode<VersusMatchPayload>(env.json)
        assertEquals("mx", decoded!!.matchId)
        assertEquals(10, decoded.durationSeconds)
    }

    @Test
    fun score_envelope_roundTrip() {
        val match = buildMatchPayload("A", 30, sampleBank(), matchId = "ms")
        val score = buildScorePayload(match, "B", isHost = false, attempted = 2, correct = 2)
        assertEquals(100, score.overallScore)
        val bytes = VersusWire.pack(VersusWire.TYPE_SCORE, VersusWire.encode(score))
        val decoded = VersusWire.decode<VersusScorePayload>(VersusWire.unpack(bytes)!!.json)
        assertEquals(score, decoded)
    }

    @Test
    fun winner_moreCorrectWins_accuracyTiebreak_draw() {
        assertEquals(VersusResult.HOST_WINS, decideVersusWinner(5, 3, 5, 5))
        assertEquals(VersusResult.GUEST_WINS, decideVersusWinner(2, 4, 5, 5))
        // Same correct: fewer attempts (more accurate) wins.
        assertEquals(VersusResult.HOST_WINS, decideVersusWinner(4, 4, 4, 6))
        assertEquals(VersusResult.GUEST_WINS, decideVersusWinner(4, 4, 7, 5))
        assertEquals(VersusResult.DRAW, decideVersusWinner(3, 3, 5, 5))
    }
}
