package com.istqb.examsimulator.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.istqb.examsimulator.data.model.Answer
import com.istqb.examsimulator.data.model.ExamAttempt
import com.istqb.examsimulator.data.model.Question
import com.istqb.examsimulator.data.repository.ExamRepository
import com.istqb.examsimulator.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewItem(
    val question: Question,
    val userAnswer: Answer?,
    val isCorrect: Boolean
)

class ReviewViewModel(
    private val examRepository: ExamRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _reviewItems = MutableStateFlow<List<ReviewItem>>(emptyList())
    val reviewItems: StateFlow<List<ReviewItem>> = _reviewItems.asStateFlow()

    private val _attempt = MutableStateFlow<ExamAttempt?>(null)
    val attempt: StateFlow<ExamAttempt?> = _attempt.asStateFlow()

    fun loadReviewData(attemptId: String) {
        viewModelScope.launch {
            val result = examRepository.getAttemptWithAnswers(attemptId)
            if (result != null) {
                val (attempt, answers) = result
                _attempt.value = attempt

                val questions = questionRepository.getQuestionsBySets(attempt.sourceFiles)
                val answersMap = answers.associateBy { it.questionId }

                val items = questions.map { question ->
                    val userAnswer = answersMap[question.id]
                    val userSelected = userAnswer?.selectedOptions?.sorted() ?: emptyList()
                    val correctAnswers = question.answer.sorted()
                    val isCorrect = userSelected == correctAnswers

                    ReviewItem(
                        question = question,
                        userAnswer = userAnswer,
                        isCorrect = isCorrect
                    )
                }

                _reviewItems.value = items
            }
        }
    }
}

