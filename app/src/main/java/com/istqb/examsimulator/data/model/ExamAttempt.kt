package com.istqb.examsimulator.data.model

data class ExamAttempt(
    val attemptId: String,
    val createdAt: Long,
    val totalQuestions: Int,
    val correct: Int,
    val wrong: Int,
    val blank: Int,
    val successPercent: Double,
    val passThresholdPercent: Int,
    val passed: Boolean,
    val durationMinutes: Int,
    val timeUsedMinutes: Int,
    val sourceFiles: List<String>,
    val seed: Long?
)

data class ExamConfig(
    val totalQuestions: Int = 40,
    val durationMinutes: Int = 60,
    val passPercent: Int = 65,
    val shuffleQuestions: Boolean = true,
    val shuffleOptions: Boolean = true,
    val seed: Long? = null,
    val selectedSets: List<String> = emptyList(),
    val isPracticeMode: Boolean = false,
    val unlimitedTime: Boolean = false
)

data class Answer(
    val questionId: Int,
    val setSource: String,
    val selectedOptions: List<String>,
    val isFlagged: Boolean = false
)

