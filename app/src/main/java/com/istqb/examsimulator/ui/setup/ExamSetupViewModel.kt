package com.istqb.examsimulator.ui.setup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.istqb.examsimulator.data.model.ExamConfig
import com.istqb.examsimulator.data.model.QuestionSet
import com.istqb.examsimulator.data.repository.QuestionRepository
import com.istqb.examsimulator.util.Shuffler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ExamSetupViewModel(
    private val questionRepository: QuestionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _availableSets = MutableStateFlow<List<QuestionSet>>(emptyList())
    val availableSets: StateFlow<List<QuestionSet>> = _availableSets.asStateFlow()

    private val _selectedSets = MutableStateFlow<Set<String>>(emptySet())
    val selectedSets: StateFlow<Set<String>> = _selectedSets.asStateFlow()

    private val _totalQuestions = MutableStateFlow(40)
    val totalQuestions: StateFlow<Int> = _totalQuestions.asStateFlow()

    private val _durationMinutes = MutableStateFlow(60)
    val durationMinutes: StateFlow<Int> = _durationMinutes.asStateFlow()

    private val _passPercent = MutableStateFlow(65)
    val passPercent: StateFlow<Int> = _passPercent.asStateFlow()

    private val _shuffleQuestions = MutableStateFlow(true)
    val shuffleQuestions: StateFlow<Boolean> = _shuffleQuestions.asStateFlow()

    private val _shuffleOptions = MutableStateFlow(true)
    val shuffleOptions: StateFlow<Boolean> = _shuffleOptions.asStateFlow()

    private val _seed = MutableStateFlow<Long?>(null)
    val seed: StateFlow<Long?> = _seed.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadQuestionSets()
        if (_seed.value == null) {
            _seed.value = Shuffler.generateSeed()
        }
    }

    fun loadQuestionSets() {
        viewModelScope.launch {
            questionRepository.getAllQuestionSets().collect { sets ->
                _availableSets.value = sets
                if (_selectedSets.value.isEmpty() && sets.isNotEmpty()) {
                    _selectedSets.value = setOf(sets.first().sourceFileName)
                }
            }
        }
    }

    fun toggleSetSelection(setFileName: String) {
        val current = _selectedSets.value.toMutableSet()
        if (current.contains(setFileName)) {
            current.remove(setFileName)
        } else {
            current.add(setFileName)
        }
        _selectedSets.value = current
    }

    fun setTotalQuestions(count: Int) {
        _totalQuestions.value = count.coerceIn(1, 1000)
    }

    fun setDurationMinutes(minutes: Int) {
        _durationMinutes.value = minutes.coerceIn(1, 480)
    }

    fun setPassPercent(percent: Int) {
        _passPercent.value = percent.coerceIn(0, 100)
    }

    fun setShuffleQuestions(shuffle: Boolean) {
        _shuffleQuestions.value = shuffle
    }

    fun setShuffleOptions(shuffle: Boolean) {
        _shuffleOptions.value = shuffle
    }

    fun setSeed(newSeed: Long?) {
        _seed.value = newSeed
    }

    fun generateSeed() {
        _seed.value = Shuffler.generateSeed()
    }

    suspend fun startExam(isPracticeMode: Boolean): Pair<String, ExamConfig>? {
        if (_selectedSets.value.isEmpty()) {
            _error.value = "En az bir soru seti seçmelisiniz"
            return null
        }

        _isLoading.value = true
        _error.value = null

        return try {
            val questions = questionRepository.getQuestionsBySets(_selectedSets.value.toList())
            if (questions.isEmpty()) {
                _error.value = "Seçilen setlerde soru bulunamadı"
                null
            } else {
                val config = ExamConfig(
                    totalQuestions = _totalQuestions.value,
                    durationMinutes = _durationMinutes.value,
                    passPercent = _passPercent.value,
                    shuffleQuestions = _shuffleQuestions.value,
                    shuffleOptions = _shuffleOptions.value,
                    seed = _seed.value,
                    selectedSets = _selectedSets.value.toList(),
                    isPracticeMode = isPracticeMode,
                    unlimitedTime = isPracticeMode
                )
                val attemptId = UUID.randomUUID().toString()
                Pair(attemptId, config)
            }
        } catch (e: Exception) {
            _error.value = "Hata: ${e.message}"
            null
        } finally {
            _isLoading.value = false
        }
    }
}

