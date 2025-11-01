package com.istqb.examsimulator.util

import com.istqb.examsimulator.data.model.Question
import com.istqb.examsimulator.data.serializer.QuestionJson
import com.istqb.examsimulator.data.serializer.QuestionJsonResponse
import kotlinx.serialization.json.Json

object JsonParser {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun parseQuestionJson(jsonString: String): QuestionJsonResponse {
        return json.decodeFromString(jsonString)
    }

    fun convertToQuestion(questionJson: QuestionJson, setSource: String): Question? {
        // Validate required fields
        if (questionJson.options.isEmpty()) {
            return null
        }
        if (questionJson.answer.isEmpty()) {
            return null
        }

        return Question(
            id = questionJson.id,
            type = questionJson.type.lowercase(),
            text = questionJson.text,
            options = questionJson.options,
            answer = questionJson.answer,
            lo = questionJson.lo,
            kLevel = questionJson.kLevel,
            score = questionJson.score,
            image = questionJson.image,
            setSource = setSource
        )
    }

    fun parseQuestionsFromJson(jsonString: String, setSource: String): List<Question> {
        val response = parseQuestionJson(jsonString)
        return response.questions.mapNotNull { questionJson ->
            convertToQuestion(questionJson, setSource)
        }
    }
}

