package com.example.inscit.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inscit.models.Lang
import com.example.inscit.xp.PendingXpBuffer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class QuizEvent {
    data class TriggerVibration(val type: String) : QuizEvent()
}

class QuizViewModel(
    private val engine: QuizEngine = QuizEngine()
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QuizEvent>()
    val events = _events.asSharedFlow()

    private val xpBuffer = PendingXpBuffer()
    val pendingXp: Int get() = xpBuffer.pendingXp

    private var questions: List<ScienceQuestion> = emptyList()
    private val userAnswers = mutableMapOf<String, QuizOption>()
    private var currentLang: Lang = Lang.EN

    private var lastCount: Int = 10
    private var lastDifficulty: String? = null
    private var lastRound: Int? = null

    fun startQuiz(lang: Lang, count: Int = 10, difficulty: String? = null, context: android.content.Context? = null) {
        if (_uiState.value is QuizUiState.QuizInProgress) return
        currentLang = lang
        lastCount = count
        lastDifficulty = difficulty
        lastRound = null
        xpBuffer.clear()
        userAnswers.clear()
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            // assets/questions.json first (EN), hardcoded fallback - no Context stored, no leak
            questions = engine.getQuestions(lang, count, difficulty, emptySet(), context)
            if (questions.isNotEmpty()) {
                _uiState.value = QuizUiState.QuizInProgress(
                    currentQuestion = questions.first(),
                    currentIndex = 0,
                    totalQuestions = questions.size
                )
            } else {
                _uiState.value = QuizUiState.Error("No questions found.")
            }
        }
    }

    fun startRoundQuiz(lang: Lang, round: Int) {
        if (_uiState.value is QuizUiState.QuizInProgress && lastRound == round) return
        currentLang = lang
        lastRound = round
        xpBuffer.clear()
        userAnswers.clear()
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            questions = engine.getDailyRoundQuestions(round, lang)
            if (questions.isNotEmpty()) {
                _uiState.value = QuizUiState.QuizInProgress(
                    currentQuestion = questions.first(),
                    currentIndex = 0,
                    totalQuestions = questions.size
                )
            } else {
                _uiState.value = QuizUiState.Error("No questions found.")
            }
        }
    }

    fun answerQuestion(optionId: Int) {
        val currentState = _uiState.value
        if (currentState !is QuizUiState.QuizInProgress || currentState.isTransitioning) return

        val selectedOption = currentState.currentQuestion.options.find { it.id == optionId } ?: return
        val isCorrect = selectedOption.isCorrect

        userAnswers[currentState.currentQuestion.id] = selectedOption

        // XP economy: +10 correct, -2 wrong (was -5, too punitive)
        xpBuffer.add(if (isCorrect) 10 else -2)

        viewModelScope.launch {
            _events.emit(QuizEvent.TriggerVibration(if (isCorrect) "SUCCESS" else "FAILURE"))

            // Show selection and transition
            _uiState.value = currentState.copy(selectedOptionId = optionId, isTransitioning = true)
            
            delay(600) // Feedback pause

            val nextIndex = currentState.currentIndex + 1
            if (nextIndex < questions.size) {
                _uiState.value = QuizUiState.QuizInProgress(
                    currentQuestion = questions[nextIndex],
                    currentIndex = nextIndex,
                    totalQuestions = questions.size
                )
            } else {
                val analytics = engine.calculateAnalytics(questions, userAnswers)
                _uiState.value = QuizUiState.Completed(analytics)
            }
        }
    }

    fun getFinalXp() = maxOf(a = 0, b = xpBuffer.pendingXp)

    // XP economy: +20 perfect bonus at 100%, 1.2x streak multiplier when streak>=3
    fun getFinalXp(streak: Int, scorePercent: Int): Int {
        var xp = xpBuffer.pendingXp
        if (scorePercent >= 100) xp += 20
        if (streak >= 3) xp = (xp * 1.2f).toInt()
        return maxOf(0, xp)
    }

    fun startQuizWithWeakDomains(lang: Lang, weakDomains: Set<ScienceDomain>, context: android.content.Context? = null) {
        if (_uiState.value is QuizUiState.QuizInProgress) return
        currentLang = lang
        xpBuffer.clear()
        userAnswers.clear()
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            questions = engine.getQuestions(lang, 10, lastDifficulty, weakDomains, context)
            if (questions.isNotEmpty()) {
                _uiState.value = QuizUiState.QuizInProgress(questions.first(), 0, questions.size)
            } else {
                _uiState.value = QuizUiState.Error("No weak-domain questions.")
            }
        }
    }

    fun retry() {
        // Reuse the already-loaded bank (assets or hardcoded) - no reshuffle surprise, no Context needed
        if (lastRound != null) {
            _uiState.value = QuizUiState.Loading
            startRoundQuiz(currentLang, lastRound!!)
            return
        }
        if (questions.isNotEmpty()) {
            xpBuffer.clear()
            userAnswers.clear()
            _uiState.value = QuizUiState.QuizInProgress(questions.first(), 0, questions.size)
        } else {
            startQuiz(currentLang, lastCount, lastDifficulty)
        }
    }
}
