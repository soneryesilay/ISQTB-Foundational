package com.istqb.examsimulator.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.istqb.examsimulator.data.model.ExamAttempt
import com.istqb.examsimulator.data.repository.ExamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel(
    private val examRepository: ExamRepository
) : ViewModel() {

    private val _attempt = MutableStateFlow<ExamAttempt?>(null)
    val attempt: StateFlow<ExamAttempt?> = _attempt.asStateFlow()

    fun loadAttempt(attemptId: String) {
        viewModelScope.launch {
            val result = examRepository.getAttemptWithAnswers(attemptId)
            result?.first?.let { _attempt.value = it }
        }
    }
}

