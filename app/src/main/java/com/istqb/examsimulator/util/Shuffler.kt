package com.istqb.examsimulator.util

import com.istqb.examsimulator.data.model.Question
import kotlin.random.Random

object Shuffler {
    fun shuffleQuestions(questions: List<Question>, seed: Long?): List<Question> {
        val random = seed?.let { Random(it) } ?: Random.Default
        return questions.shuffled(random)
    }

    fun shuffleOptions(question: Question, seed: Long?): Question {
        val random = seed?.let { Random(it + question.id) } ?: Random.Default
        val shuffledOptions = question.options.entries.shuffled(random).associate { it.key to it.value }
        
        // Maintain answer mapping after shuffle
        val originalKeys = question.options.keys.toList()
        val shuffledKeys = shuffledOptions.keys.toList()
        val answerMapping = originalKeys.zip(shuffledKeys).toMap()
        val shuffledAnswers = question.answer.map { answer ->
            answerMapping[answer] ?: answer
        }

        return question.copy(
            options = shuffledOptions,
            answer = shuffledAnswers
        )
    }

    fun generateSeed(): Long {
        return System.currentTimeMillis()
    }
}

