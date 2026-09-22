package com.example.inscit.quiz

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inscit.models.Lang
import com.example.inscit.xp.PendingXpBuffer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class SprintEvent {
    data class TriggerVibration(val type: String) : SprintEvent()
}

// Rapid infinite-Q round: questions cycle endlessly until the clock hits zero.
class SprintViewModel(
    private val engine: QuizEngine = QuizEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow<SprintUiState>(SprintUiState.Idle)
    val uiState: StateFlow<SprintUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SprintEvent>()
    val events = _events.asSharedFlow()

    private val xpBuffer = PendingXpBuffer()
    val pendingXp: Int get() = xpBuffer.pendingXp

    private var bank: List<ScienceQuestion> = emptyList()
    private var cursor = 0
    private val userAnswers = mutableMapOf<String, QuizOption>()
    private val answeredQuestions = mutableListOf<ScienceQuestion>()
    private var timerJob: Job? = null
    private var currentLang: Lang = Lang.EN
    private var duration: SprintDuration = SprintDuration.SEC_30

    fun start(lang: Lang, duration: SprintDuration, context: Context?) {
        if (_uiState.value is SprintUiState.Running) return
        currentLang = lang
        this.duration = duration
        xpBuffer.clear()
        userAnswers.clear()
        answeredQuestions.clear()
        cursor = 0
        // Whole prime bank, reshuffled when exhausted = infinite questions.
        bank = engine.getQuestions(lang, Int.MAX_VALUE, null, emptySet(), context)
        if (bank.isEmpty()) bank = engine.getQuestions(lang, Int.MAX_VALUE, null)
        viewModelScope.launch {
            _uiState.value = SprintUiState.Running(
                currentQuestion = bank.first(),
                answered = 0,
                correct = 0,
                timeLeftMs = duration.seconds * 1000L,
                totalMs = duration.seconds * 1000L
            )
            startTimer(duration.seconds * 1000L)
        }
    }

    private fun startTimer(totalMs: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val tickMs = 100L
            var left = totalMs
            while (left > 0 && isActive) {
                delay(tickMs)
                left -= tickMs
                val s = _uiState.value
                if (s is SprintUiState.Running) {
                    _uiState.value = s.copy(timeLeftMs = maxOf(0L, left))
                } else return@launch
            }
            finish()
        }
    }

    fun answerQuestion(optionId: Int) {
        val current = _uiState.value
        if (current !is SprintUiState.Running || current.isTransitioning) return
        val selected = current.currentQuestion.options.find { it.id == optionId } ?: return
        val isCorrect = selected.isCorrect
        userAnswers[current.currentQuestion.id] = selected
        answeredQuestions.add(current.currentQuestion)
        // Same economy as normal quizzes: +10 / -2.
        xpBuffer.add(if (isCorrect) 10 else -2)
        viewModelScope.launch {
            _events.emit(SprintEvent.TriggerVibration(if (isCorrect) "SUCCESS" else "FAILURE"))
            _uiState.value = current.copy(selectedOptionId = optionId, isTransitioning = true)
            delay(300) // rapid feel for bullet rounds (normal quiz uses 600ms)
            val now = _uiState.value
            if (now !is SprintUiState.Running) return@launch
            cursor++
            if (cursor >= bank.size) {
                bank = bank.shuffled()
                cursor = 0
            }
            _uiState.value = now.copy(
                currentQuestion = bank[cursor],
                answered = now.answered + 1,
                correct = now.correct + if (isCorrect) 1 else 0,
                selectedOptionId = null,
                isTransitioning = false
            )
        }
    }

    private fun finish() {
        val s = _uiState.value
        if (s !is SprintUiState.Running) return
        timerJob?.cancel()
        timerJob = null
        val answeredQs = answeredQuestions.toList()
        val base = try {
            engine.calculateAnalytics(answeredQs, userAnswers)
        } catch (_: Exception) {
            null
        }
        val correct = s.correct
        val attempted = s.answered
        val score = if (attempted == 0) 0 else ((correct.toFloat() / attempted) * 100).toInt()
        _uiState.value = SprintUiState.Finished(
            SprintAnalytics(
                attempted = attempted,
                correct = correct,
                wrong = attempted - correct,
                overallScore = score,
                radarData = base?.radarData ?: emptyList(),
                strengthsEn = base?.strengthsEn ?: emptyList(),
                strengthsHi = base?.strengthsHi ?: emptyList(),
                weaknessesEn = base?.weaknessesEn ?: emptyList(),
                weaknessesHi = base?.weaknessesHi ?: emptyList(),
                explanations = base?.explanations ?: emptyList()
            )
        )
    }

    fun getFinalXp(streak: Int = 0, scorePercent: Int = 0): Int {
        var xp = xpBuffer.pendingXp
        if (scorePercent == 100 && xp > 0) xp += 20
        if (streak >= 3) xp = (xp * 1.2f).toInt()
        return maxOf(0, xp)
    }

    fun cancel() {
        timerJob?.cancel()
        timerJob = null
        if (_uiState.value is SprintUiState.Running) {
            finish()
        }
    }

    fun reset() {
        timerJob?.cancel()
        timerJob = null
        xpBuffer.clear()
        userAnswers.clear()
        answeredQuestions.clear()
        cursor = 0
        _uiState.value = SprintUiState.Idle
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }
}
