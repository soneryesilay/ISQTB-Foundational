package com.istqb.examsimulator.ui.questiondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.istqb.examsimulator.data.model.Question
import com.istqb.examsimulator.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuestionDetailViewModel(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _selectedQuestion = MutableStateFlow<Question?>(null)
    val selectedQuestion: StateFlow<Question?> = _selectedQuestion.asStateFlow()

    fun loadQuestions(questionSetId: String) {
        viewModelScope.launch {
            questionRepository.getQuestionsBySet(questionSetId).collect { questionList ->
                _questions.value = questionList
                if (_selectedQuestion.value == null && questionList.isNotEmpty()) {
                    _selectedQuestion.value = questionList.first()
                }
            }
        }
    }

    fun selectQuestion(question: Question) {
        _selectedQuestion.value = question
    }

    fun deleteQuestion(questionId: Int) {
        viewModelScope.launch {
            questionRepository.deleteQuestion(questionId.toString())
            _selectedQuestion.value = null
        }
    }

    fun updateQuestion(question: Question) {
        viewModelScope.launch {
            questionRepository.updateQuestion(question)
            // Refresh the selected question
            _selectedQuestion.value = question
        }
    }
}
