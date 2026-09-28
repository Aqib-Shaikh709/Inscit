package com.example.inscit.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inscit.transfer.VersusMatchPayload
import com.example.inscit.transfer.toQuestion
import com.example.inscit.xp.PendingXpBuffer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class VersusRoundState {
    object Idle : VersusRoundState()
    data class Countdown(val secondsLeft: Int, val totalSeconds: Int = 3) : VersusRoundState()
    data class Running(
        val questions: List<ScienceQuestion> = emptyList(),
        val index: Int = 0,
        val currentQuestion: ScienceQuestion? = null,
        val selectedOptionId: Int? = null,
        val isTransitioning: Boolean = false,
        val timeLeftMs: Long = 0L,
        val totalMs: Long = 0L,
        val answered: Int = 0,
        val correct: Int = 0
    ) : VersusRoundState()
    data class Finished(
        val attempted: Int,
        val correct: Int,
        val overallScore: Int
    ) : VersusRoundState()
}

// Independent local match: both devices run the same fixed bank + timer, evaluated locally.
// Host builds the bank from QuizEngine; guest adopts the received MATCH payload verbatim
// (same order = fair fight). No server, no internet.
class VersusMatchViewModel : ViewModel() {

    private val _round = MutableStateFlow<VersusRoundState>(VersusRoundState.Idle)
    val round: StateFlow<VersusRoundState> = _round.asStateFlow()

    private val xpBuffer = PendingXpBuffer()
    val pendingXp: Int get() = xpBuffer.pendingXp

    private var questions: List<ScienceQuestion> = emptyList()
    private var durationMs: Long = 30_000L
    private var userAnswers = mutableMapOf<String, QuizOption>()

    private var timerJob: Job? = null
    private var countdownJob: Job? = null

    var match: VersusMatchPayload? = null
        private set
    var isHost: Boolean = true
        private set

    fun setMatch(payload: VersusMatchPayload, host: Boolean) {
        match = payload
        isHost = host
        questions = payload.questions.map { it.toQuestion() }
        durationMs = (payload.durationSeconds.coerceIn(5, 300)) * 1000L
        userAnswers.clear()
        xpBuffer.clear()
        timerJob?.cancel()
        countdownJob?.cancel()
        _round.value = VersusRoundState.Idle
    }

    // Synced simultaneous start: both sides count down N s, then the local timer runs.
    fun startCountdown(seconds: Int = 3) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            val total = seconds.coerceIn(1, 10)
            for (s in total downTo 1) {
                _round.value = VersusRoundState.Countdown(s, total)
                delay(1000)
            }
            beginRound()
        }
    }

    private fun beginRound() {
        if (questions.isEmpty()) {
            _round.value = VersusRoundState.Finished(0, 0, 0)
            return
        }
        userAnswers.clear()
        xpBuffer.clear()
        _round.value = VersusRoundState.Running(
            questions = questions,
            index = 0,
            currentQuestion = questions[0],
            timeLeftMs = durationMs,
            totalMs = durationMs
        )
        startTimer(durationMs)
    }

    private fun startTimer(totalMs: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var left = totalMs
            val tick = 100L
            while (left > 0 && isActive) {
                delay(tick)
                left -= tick
                val s = _round.value
                if (s is VersusRoundState.Running) {
                    _round.value = s.copy(timeLeftMs = maxOf(0L, left))
                } else return@launch
            }
            finish()
        }
    }

    fun answerQuestion(optionId: Int) {
        val s = _round.value
        if (s !is VersusRoundState.Running || s.isTransitioning) return
        val q = s.currentQuestion ?: return
        val selected = q.options.find { it.id == optionId } ?: return
        val ok = selected.isCorrect
        userAnswers[q.id] = selected
        xpBuffer.add(if (ok) 10 else -2)
        viewModelScope.launch {
            _round.value = s.copy(selectedOptionId = optionId, isTransitioning = true)
            delay(350)
            val now = _round.value
            if (now !is VersusRoundState.Running) return@launch
            val nextIndex = now.index + 1
            if (nextIndex >= now.questions.size) {
                // All questions answered before time: finish early (independent local match).
                finishLocked(now.answered + 1, now.correct + if (ok) 1 else 0)
            } else {
                _round.value = now.copy(
                    index = nextIndex,
                    currentQuestion = now.questions[nextIndex],
                    answered = now.answered + 1,
                    correct = now.correct + if (ok) 1 else 0,
                    selectedOptionId = null,
                    isTransitioning = false
                )
            }
        }
    }

    // "I don't know" (SKIP) is a deliberate dodge, not a free pass: it costs the same
    // -2 XP as a wrong answer and counts as attempted-but-missed, so it drags your %
    // and the accuracy tiebreak. Its only edge over guessing is speed (no 350ms verdict
    // pause). Without this, skipping every hard Q would farm accuracy for free.
    fun skipQuestion() {
        val s = _round.value
        if (s !is VersusRoundState.Running || s.isTransitioning) return
        xpBuffer.add(-2)
        val attempted = s.answered + 1
        val nextIndex = s.index + 1
        if (nextIndex >= s.questions.size) {
            finishLocked(attempted, s.correct)
        } else {
            _round.value = s.copy(
                index = nextIndex,
                currentQuestion = s.questions[nextIndex],
                answered = attempted,
                selectedOptionId = null,
                isTransitioning = false
            )
        }
    }

    private fun finishLocked(answered: Int, correct: Int) {
        timerJob?.cancel()
        timerJob = null
        val score = if (answered == 0) 0 else ((correct.toFloat() / answered) * 100).toInt()
        _round.value = VersusRoundState.Finished(answered, correct, score)
    }

    fun finish() {
        val s = _round.value
        if (s is VersusRoundState.Finished) return
        if (s is VersusRoundState.Running) {
            finishLocked(s.answered, s.correct)
        } else {
            finishLocked(0, 0)
        }
    }

    // Same economy as Sprint (+10/-2, +20 @100%, x1.2 @streak>=3) + winner bonus.
    fun getFinalXp(streak: Int = 0, scorePercent: Int = 0, won: Boolean = false): Int {
        var xp = xpBuffer.pendingXp
        if (scorePercent == 100 && xp > 0) xp += 20
        if (won) xp += 15
        if (streak >= 3) xp = (xp * 1.2f).toInt()
        return maxOf(0, xp)
    }

    fun cancel() {
        timerJob?.cancel()
        countdownJob?.cancel()
        timerJob = null
        countdownJob = null
        if (_round.value is VersusRoundState.Running || _round.value is VersusRoundState.Countdown) {
            finish()
        }
    }

    fun reset() {
        timerJob?.cancel()
        countdownJob?.cancel()
        timerJob = null
        countdownJob = null
        xpBuffer.clear()
        userAnswers.clear()
        questions = emptyList()
        match = null
        _round.value = VersusRoundState.Idle
    }
}
