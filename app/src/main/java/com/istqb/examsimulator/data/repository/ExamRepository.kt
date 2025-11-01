package com.istqb.examsimulator.data.repository

import com.istqb.examsimulator.data.local.database.ExamDao
import com.istqb.examsimulator.data.local.entities.AttemptAnswerEntity
import com.istqb.examsimulator.data.local.entities.ExamAttemptEntity
import com.istqb.examsimulator.data.model.Answer
import com.istqb.examsimulator.data.model.ExamAttempt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExamRepository(
    private val examDao: ExamDao
) {
    suspend fun saveAttempt(attempt: ExamAttempt, answers: List<Answer>) {
        val attemptEntity = ExamAttemptEntity(
            attemptId = attempt.attemptId,
            createdAt = attempt.createdAt,
            totalQuestions = attempt.totalQuestions,
            correct = attempt.correct,
            wrong = attempt.wrong,
            blank = attempt.blank,
            successPercent = attempt.successPercent,
            passThresholdPercent = attempt.passThresholdPercent,
            passed = attempt.passed,
            durationMinutes = attempt.durationMinutes,
            timeUsedMinutes = attempt.timeUsedMinutes,
            sourceFiles = attempt.sourceFiles,
            seed = attempt.seed
        )

        examDao.insertAttempt(attemptEntity)

        val answerEntities = answers.map { answer ->
            AttemptAnswerEntity(
                attemptId = attempt.attemptId,
                questionId = answer.questionId,
                selectedOptions = answer.selectedOptions,
                isCorrect = false, // Will be calculated during evaluation
                isFlagged = answer.isFlagged
            )
        }
        examDao.insertAnswers(answerEntities)
    }

    fun getRecentAttempts(limit: Int = 5): Flow<List<ExamAttempt>> {
        return examDao.getRecentAttempts(limit).map { entities ->
            entities.map { entity ->
                ExamAttempt(
                    attemptId = entity.attemptId,
                    createdAt = entity.createdAt,
                    totalQuestions = entity.totalQuestions,
                    correct = entity.correct,
                    wrong = entity.wrong,
                    blank = entity.blank,
                    successPercent = entity.successPercent,
                    passThresholdPercent = entity.passThresholdPercent,
                    passed = entity.passed,
                    durationMinutes = entity.durationMinutes,
                    timeUsedMinutes = entity.timeUsedMinutes,
                    sourceFiles = entity.sourceFiles,
                    seed = entity.seed
                )
            }
        }
    }

    suspend fun getAttemptWithAnswers(attemptId: String): Pair<ExamAttempt, List<Answer>>? {
        val result = examDao.getAttemptWithAnswers(attemptId) ?: return null

        val attempt = ExamAttempt(
            attemptId = result.attempt.attemptId,
            createdAt = result.attempt.createdAt,
            totalQuestions = result.attempt.totalQuestions,
            correct = result.attempt.correct,
            wrong = result.attempt.wrong,
            blank = result.attempt.blank,
            successPercent = result.attempt.successPercent,
            passThresholdPercent = result.attempt.passThresholdPercent,
            passed = result.attempt.passed,
            durationMinutes = result.attempt.durationMinutes,
            timeUsedMinutes = result.attempt.timeUsedMinutes,
            sourceFiles = result.attempt.sourceFiles,
            seed = result.attempt.seed
        )

        val answers = result.answers.map { entity ->
            Answer(
                questionId = entity.questionId,
                selectedOptions = entity.selectedOptions,
                isFlagged = entity.isFlagged
            )
        }

        return Pair(attempt, answers)
    }

    fun getAverageSuccessPercent(): Flow<Double?> {
        return examDao.getAverageSuccessPercent()
    }

    fun getTotalAttemptCount(): Flow<Int> {
        return examDao.getTotalAttemptCount()
    }
}

