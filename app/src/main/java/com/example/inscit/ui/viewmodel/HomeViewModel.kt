package com.example.inscit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inscit.data.repository.UserRepository
import com.example.inscit.models.UserDocument
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: UserRepository) : ViewModel() {

    private val _document = MutableStateFlow<UserDocument?>(null)
    val document: StateFlow<UserDocument?> = _document

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init {
        viewModelScope.launch {
            _loading.value = true
            repository.observeDocument().collect { doc ->
                _document.value = doc
                _loading.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _document.value = repository.getDocument()
        }
    }

    fun save(doc: UserDocument) {
        repository.saveDocument(doc)
        _document.value = doc
    }

    fun currentStreak(): Int = _document.value?.stats?.currentStreak ?: 0
    fun currentXp(): Int = _document.value?.stats?.xp ?: 0
    fun currentLevel(): Int = (currentXp() / 100) + 1
    fun lastQuizScore(): Float = _document.value?.quizProgress?.lastScore ?: 0f
    fun completedGoals(): Int = _document.value?.goals?.count { it.isCompleted } ?: 0
    fun totalGoals(): Int = _document.value?.goals?.size ?: 0
    fun completedChallengeDays(): Int = _document.value?.stats?.completedChallengeDates?.size ?: 0
}
