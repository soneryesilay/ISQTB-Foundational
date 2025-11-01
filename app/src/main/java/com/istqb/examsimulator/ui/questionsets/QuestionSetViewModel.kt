package com.istqb.examsimulator.ui.questionsets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.istqb.examsimulator.data.model.QuestionSet
import com.istqb.examsimulator.data.repository.QuestionRepository
import com.istqb.examsimulator.data.serializer.QuestionJsonResponse
import com.istqb.examsimulator.util.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class QuestionSetViewModel(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    val questionSets: StateFlow<List<QuestionSet>> = questionRepository.getAllQuestionSets()
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun importQuestionSet(jsonString: String, fileName: String) {
        viewModelScope.launch {
            try {
                val response = JsonParser.parseQuestionJson(jsonString)
                val questions = JsonParser.parseQuestionsFromJson(jsonString, fileName)

                if (questions.isEmpty()) {
                    _error.value = "Soru bulunamadı"
                    return@launch
                }

                // Check if set already exists
                val existingSet = questionRepository.questionSetDao.getSetByFileName(fileName)
                if (existingSet != null) {
                    // Update existing set
                    questionRepository.insertQuestions(questions, fileName)
                    val updatedSet = QuestionSet(
                        sourceFileName = fileName,
                        title = response.meta.title,
                        version = response.meta.version,
                        lang = response.meta.lang,
                        importTimestamp = System.currentTimeMillis(),
                        questionCount = questions.size
                    )
                    questionRepository.insertQuestionSet(updatedSet)
                } else {
                    // Insert new set
                    questionRepository.insertQuestions(questions, fileName)
                    val newSet = QuestionSet(
                        sourceFileName = fileName,
                        title = response.meta.title,
                        version = response.meta.version,
                        lang = response.meta.lang,
                        importTimestamp = System.currentTimeMillis(),
                        questionCount = questions.size
                    )
                    questionRepository.insertQuestionSet(newSet)
                }

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Hata: ${e.message}"
            }
        }
    }

    fun showError(message: String) {
        _error.value = message
    }

    fun clearError() {
        _error.value = null
    }
}

