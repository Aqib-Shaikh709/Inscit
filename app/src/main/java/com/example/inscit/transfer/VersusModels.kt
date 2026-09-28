package com.example.inscit.transfer

import com.example.inscit.quiz.QuizOption
import com.example.inscit.quiz.ScienceDomain
import com.example.inscit.quiz.ScienceQuestion
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.util.UUID

// Local Versus (offline PvP) protocol models.
//
// Flow (see screenshots 1-4 + feature paragraph):
//  1. Offline handshake  - both devices discover/connect locally (BT / Wi-Fi Direct via
//     Nearby P2P_STAR, SERVICE_ID below). No internet.
//  2. Payload transfer   - Device A (host) sends JSON payload: quiz questions +
//     sprint timer config, straight from local storage (QuizEngine bank).
//  3. Independent match  - both screens start simultaneously (synced 3s countdown),
//     questions evaluated locally on each device.
//  4. Integrity check    - after finish both exchange final scores + client-side
//     SHA-256 hash; each side recomputes and marks VERIFIED / TAMPERED.
//  5. Instant result     - winner declared locally, no central server.

@Serializable
data class VersusOptionDto(
    val id: Int,
    val text: String,
    val isCorrect: Boolean
)

@Serializable
data class VersusQuestionDto(
    val id: String,
    val domain: String,
    val text: String,
    val options: List<VersusOptionDto>,
    val explanation: String = "",
    val difficulty: String = "BASIC"
)

@Serializable
data class VersusMatchPayload(
    val matchId: String,
    val hostName: String,
    val durationSeconds: Int,
    val questions: List<VersusQuestionDto>,
    val questionsHash: String
)

@Serializable
data class VersusReady(
    val matchId: String,
    val guestName: String
)

@Serializable
data class VersusStart(
    val matchId: String,
    val countdownSeconds: Int = 3
)

@Serializable
data class VersusScorePayload(
    val matchId: String,
    val deviceName: String,
    val isHost: Boolean,
    val attempted: Int,
    val correct: Int,
    val overallScore: Int,
    // SHA-256 over matchId|attempted|correct|questionsHash (client-side integrity).
    val hash: String
)

// Envelope framing for BYTES payloads: {"type": "...", "json": "..."}.
@Serializable
data class VersusEnvelope(
    val type: String,
    val json: String
)

object VersusWire {
    const val TYPE_MATCH = "VERSUS_MATCH"
    const val TYPE_READY = "VERSUS_READY"
    const val TYPE_START = "VERSUS_START"
    const val TYPE_SCORE = "VERSUS_SCORE"

    val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true; isLenient = true }

    fun pack(type: String, bodyJson: String): ByteArray =
        json.encodeToString(VersusEnvelope(type, bodyJson)).toByteArray(Charsets.UTF_8)

    fun unpack(bytes: ByteArray): VersusEnvelope? = try {
        json.decodeFromString<VersusEnvelope>(String(bytes, Charsets.UTF_8))
    } catch (_: Exception) { null }

    inline fun <reified T> decode(bodyJson: String): T? = try {
        json.decodeFromString<T>(bodyJson)
    } catch (_: Exception) { null }

    inline fun <reified T> encode(value: T): String = json.encodeToString(value)
}

object VersusCrypto {
    fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return buildString(bytes.size * 2) {
            for (b in bytes) append(String.format("%02x", b))
        }
    }

    // Stable hash of the question set both sides share (ids in order).
    fun questionsHash(questions: List<VersusQuestionDto>): String =
        sha256Hex(questions.joinToString("|") { it.id })

    // Client-side score attestation both sides recompute to detect tampering.
    fun scoreHash(matchId: String, attempted: Int, correct: Int, questionsHash: String): String =
        sha256Hex("$matchId|$attempted|$correct|$questionsHash")

    fun verifyScore(score: VersusScorePayload, matchId: String, questionsHash: String): Boolean {
        if (score.matchId != matchId) return false
        if (score.attempted < 0 || score.correct < 0 || score.correct > score.attempted) return false
        val expectedScore = if (score.attempted == 0) 0 else ((score.correct.toFloat() / score.attempted) * 100).toInt()
        if (score.overallScore != expectedScore) return false
        return score.hash == scoreHash(matchId, score.attempted, score.correct, questionsHash)
    }
}

enum class VersusResult { HOST_WINS, GUEST_WINS, DRAW }

fun decideVersusWinner(hostCorrect: Int, guestCorrect: Int, hostAttempted: Int, guestAttempted: Int): VersusResult {
    // Primary: more correct answers. Tiebreak: fewer attempts (accuracy), else draw.
    if (hostCorrect != guestCorrect) return if (hostCorrect > guestCorrect) VersusResult.HOST_WINS else VersusResult.GUEST_WINS
    if (hostAttempted != guestAttempted) return if (hostAttempted < guestAttempted) VersusResult.HOST_WINS else VersusResult.GUEST_WINS
    return VersusResult.DRAW
}

enum class VersusStage {
    IDLE, ADVERTISING, DISCOVERING, CONNECTING,
    LOBBY_HOST, LOBBY_GUEST,
    COUNTDOWN, ROUND, WAIT_PEER, RESULT, DENIED
}

// ---- Conversions between quiz bank questions and wire DTOs ----

fun ScienceQuestion.toDto(): VersusQuestionDto = VersusQuestionDto(
    id = id,
    domain = domain.name,
    text = text,
    options = options.map { VersusOptionDto(it.id, it.text, it.isCorrect) },
    explanation = explanation,
    difficulty = difficulty
)

fun VersusQuestionDto.toQuestion(): ScienceQuestion {
    val domain = try { ScienceDomain.valueOf(domain) } catch (_: Exception) { ScienceDomain.PHYSICS }
    return ScienceQuestion(
        id = id,
        domain = domain,
        text = text,
        options = options.map { QuizOption(it.id, it.text, it.isCorrect) },
        explanation = explanation,
        difficulty = difficulty
    )
}

fun buildMatchPayload(
    hostName: String,
    durationSeconds: Int,
    bank: List<ScienceQuestion>,
    matchId: String = UUID.randomUUID().toString()
): VersusMatchPayload {
    val dtos = bank.map { it.toDto() }
    return VersusMatchPayload(
        matchId = matchId,
        hostName = hostName,
        durationSeconds = durationSeconds,
        questions = dtos,
        questionsHash = VersusCrypto.questionsHash(dtos)
    )
}

fun buildScorePayload(
    match: VersusMatchPayload,
    deviceName: String,
    isHost: Boolean,
    attempted: Int,
    correct: Int
): VersusScorePayload {
    val overall = if (attempted == 0) 0 else ((correct.toFloat() / attempted) * 100).toInt()
    return VersusScorePayload(
        matchId = match.matchId,
        deviceName = deviceName,
        isHost = isHost,
        attempted = attempted,
        correct = correct,
        overallScore = overall,
        hash = VersusCrypto.scoreHash(match.matchId, attempted, correct, match.questionsHash)
    )
}
