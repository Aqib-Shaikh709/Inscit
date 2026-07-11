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

    fun startQuiz(lang: Lang, count: Int = 10, difficulty: String? = null) {
        if (_uiState.value !is QuizUiState.Loading) return
        currentLang = lang
        lastCount = count
        lastDifficulty = difficulty
        xpBuffer.clear()
        userAnswers.clear()
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            questions = engine.getQuestions(lang, count, difficulty)
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

        // Update XP Buffer only
        xpBuffer.add(if (isCorrect) 10 else -5)

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

    fun getFinalXp() = xpBuffer.pendingXp

    fun retry() {
        _uiState.value = QuizUiState.Loading
        startQuiz(currentLang, lastCount, lastDifficulty)
    }
}
