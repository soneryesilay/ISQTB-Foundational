package com.istqb.examsimulator.util

import com.istqb.examsimulator.data.model.Answer
import com.istqb.examsimulator.data.model.Question

object ExamEvaluator {
    fun evaluateAnswer(question: Question, answer: Answer): Boolean {
        if (answer.selectedOptions.isEmpty()) {
            return false // Blank answer
        }

        val correctAnswers = question.answer.sorted()
        val userAnswers = answer.selectedOptions.sorted()

        return correctAnswers == userAnswers
    }

    data class EvaluationResult(
        val correct: Int,
        val wrong: Int,
        val blank: Int,
        val successPercent: Double,
        val passed: Boolean
    )

    fun evaluateExam(
        questions: List<Question>,
        answers: Map<Int, Answer>,
        passThresholdPercent: Int
    ): EvaluationResult {
        var correct = 0
        var wrong = 0
        var blank = 0

        questions.forEach { question ->
            val answer = answers[question.id]
            if (answer == null || answer.selectedOptions.isEmpty()) {
                blank++
            } else {
                val isCorrect = evaluateAnswer(question, answer)
                if (isCorrect) {
                    correct++
                } else {
                    wrong++
                }
            }
        }

        val total = questions.size
        val successPercent = if (total > 0) {
            (correct.toDouble() / total.toDouble()) * 100.0
        } else {
            0.0
        }

        val passed = successPercent >= passThresholdPercent

        return EvaluationResult(
            correct = correct,
            wrong = wrong,
            blank = blank,
            successPercent = successPercent,
            passed = passed
        )
    }
}

