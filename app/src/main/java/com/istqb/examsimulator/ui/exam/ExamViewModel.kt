package com.istqb.examsimulator.ui.exam

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.istqb.examsimulator.data.model.Answer
import com.istqb.examsimulator.data.model.ExamConfig
import com.istqb.examsimulator.data.model.Question
import com.istqb.examsimulator.data.repository.ExamRepository
import com.istqb.examsimulator.data.repository.QuestionRepository
import com.istqb.examsimulator.util.ExamEvaluator
import com.istqb.examsimulator.util.Shuffler
import com.istqb.examsimulator.util.TimerUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ExamState(
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val answers: Map<Int, Answer> = emptyMap(),
    val flaggedQuestions: Set<Int> = emptySet(),
    val config: ExamConfig? = null,
    val timeRemainingSeconds: Long = 0,
    val isFinished: Boolean = false,
    val attemptId: String = ""
)

class ExamViewModel(
    private val questionRepository: QuestionRepository,
    private val examRepository: ExamRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val attemptId: String = savedStateHandle.get<String>("attemptId") ?: ""

    private val _examState = MutableStateFlow(ExamState(attemptId = attemptId))
    val examState: StateFlow<ExamState> = _examState.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Load will be triggered when attemptId is set via SavedStateHandle
    }

    fun loadExamData(newAttemptId: String) {
        if (newAttemptId.isEmpty() || newAttemptId == _examState.value.attemptId) return
        
        viewModelScope.launch {
            // Load config from store
            val config = com.istqb.examsimulator.util.ExamConfigStore.getConfig(newAttemptId)
                ?: return@launch // Config not found, cannot proceed
            
            val allQuestions = questionRepository.getQuestionsBySets(config.selectedSets)

            val questions = if (config.shuffleQuestions) {
                Shuffler.shuffleQuestions(allQuestions, config.seed).take(config.totalQuestions)
            } else {
                allQuestions.take(config.totalQuestions)
            }

            val shuffledQuestions = if (config.shuffleOptions) {
                questions.map { q -> Shuffler.shuffleOptions(q, config.seed) }
            } else {
                questions
            }

            val totalSeconds = if (config.unlimitedTime) Long.MAX_VALUE else config.durationMinutes * 60L

            _examState.value = _examState.value.copy(
                questions = shuffledQuestions,
                config = config,
                timeRemainingSeconds = totalSeconds,
                attemptId = newAttemptId
            )

            startTimer()
        }
    }

    fun initializeExam(config: ExamConfig, allQuestions: List<Question>) {
        viewModelScope.launch {
            val questions = if (config.shuffleQuestions) {
                Shuffler.shuffleQuestions(allQuestions, config.seed).take(config.totalQuestions)
            } else {
                allQuestions.take(config.totalQuestions)
            }

            val shuffledQuestions = if (config.shuffleOptions) {
                questions.map { q -> Shuffler.shuffleOptions(q, config.seed) }
            } else {
                questions
            }

            val totalSeconds = if (config.unlimitedTime) Long.MAX_VALUE else config.durationMinutes * 60L

            val newAttemptId = UUID.randomUUID().toString()

            _examState.value = _examState.value.copy(
                questions = shuffledQuestions,
                config = config,
                timeRemainingSeconds = totalSeconds,
                attemptId = newAttemptId
            )

            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_examState.value.timeRemainingSeconds > 0 && !_examState.value.isFinished) {
                delay(1000)
                val current = _examState.value
                if (!current.config?.unlimitedTime!! && current.timeRemainingSeconds > 0) {
                    _examState.value = current.copy(
                        timeRemainingSeconds = current.timeRemainingSeconds - 1
                    )
                } else if (current.timeRemainingSeconds <= 0) {
                    finishExam()
                    break
                }
            }
        }
    }

    fun selectAnswer(option: String, isSelected: Boolean) {
        val current = _examState.value
        val question = current.questions.getOrNull(current.currentQuestionIndex) ?: return
        val currentAnswer = current.answers[question.id] ?: Answer(question.id, emptyList())

        val newSelected = if (question.type == "multiple") {
            if (isSelected) {
                currentAnswer.selectedOptions + option
            } else {
                currentAnswer.selectedOptions - option
            }
        } else {
            if (isSelected) listOf(option) else emptyList()
        }

        val updatedAnswer = currentAnswer.copy(selectedOptions = newSelected)
        val updatedAnswers = current.answers.toMutableMap().apply {
            this[question.id] = updatedAnswer
        }

        _examState.value = current.copy(answers = updatedAnswers)
    }

    fun toggleFlag() {
        val current = _examState.value
        val question = current.questions.getOrNull(current.currentQuestionIndex) ?: return
        val flagged = current.flaggedQuestions.toMutableSet()
        if (flagged.contains(question.id)) {
            flagged.remove(question.id)
        } else {
            flagged.add(question.id)
        }
        _examState.value = current.copy(flaggedQuestions = flagged)
    }

    fun navigateToQuestion(index: Int) {
        val current = _examState.value
        if (index in current.questions.indices) {
            _examState.value = current.copy(currentQuestionIndex = index)
        }
    }

    fun nextQuestion() {
        val current = _examState.value
        if (current.currentQuestionIndex < current.questions.size - 1) {
            _examState.value = current.copy(currentQuestionIndex = current.currentQuestionIndex + 1)
        }
    }

    fun previousQuestion() {
        val current = _examState.value
        if (current.currentQuestionIndex > 0) {
            _examState.value = current.copy(currentQuestionIndex = current.currentQuestionIndex - 1)
        }
    }

    fun finishExam() {
        timerJob?.cancel()
        val current = _examState.value
        if (current.isFinished) return

        val result = ExamEvaluator.evaluateExam(
            questions = current.questions,
            answers = current.answers,
            passThresholdPercent = current.config?.passPercent ?: 65
        )

        val timeUsed = current.config?.durationMinutes?.let { duration ->
            val elapsed = duration - (current.timeRemainingSeconds / 60).toInt()
            elapsed.coerceAtLeast(0)
        } ?: 0

        viewModelScope.launch {
            val attempt = com.istqb.examsimulator.data.model.ExamAttempt(
                attemptId = current.attemptId,
                createdAt = System.currentTimeMillis(),
                totalQuestions = current.questions.size,
                correct = result.correct,
                wrong = result.wrong,
                blank = result.blank,
                successPercent = result.successPercent,
                passThresholdPercent = current.config?.passPercent ?: 65,
                passed = result.passed,
                durationMinutes = current.config?.durationMinutes ?: 60,
                timeUsedMinutes = timeUsed,
                sourceFiles = current.config?.selectedSets ?: emptyList(),
                seed = current.config?.seed
            )

            val answersList = current.questions.map { question ->
                val answer = current.answers[question.id] ?: Answer(question.id, emptyList())
                answer.copy(isFlagged = current.flaggedQuestions.contains(question.id))
            }

            examRepository.saveAttempt(attempt, answersList)

            _examState.value = current.copy(isFinished = true)
        }
    }

    fun getFormattedTimeRemaining(): String {
        return TimerUtils.formatTime(_examState.value.timeRemainingSeconds)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

