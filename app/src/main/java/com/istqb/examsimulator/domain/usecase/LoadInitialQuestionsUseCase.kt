package com.istqb.examsimulator.domain.usecase

import android.content.Context
import com.istqb.examsimulator.data.model.QuestionSet
import com.istqb.examsimulator.data.repository.QuestionRepository
import com.istqb.examsimulator.data.serializer.QuestionJsonResponse
import com.istqb.examsimulator.util.AssetLoader
import com.istqb.examsimulator.util.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadInitialQuestionsUseCase(
    private val questionRepository: QuestionRepository,
    private val context: Context
) {
    private val initialAssets = listOf(
        "istqb_sample_a.json",
        "istqb_sample_b.json"
    )

    suspend fun execute(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var totalLoaded = 0

            initialAssets.forEach { assetName ->
                val jsonString = AssetLoader.loadAssetAsString(context, assetName)
                    ?: return@withContext Result.failure(Exception("Failed to load $assetName"))

                val response = JsonParser.parseQuestionJson(jsonString)
                val questions = JsonParser.parseQuestionsFromJson(jsonString, assetName)

                // Only insert if set doesn't exist or questions are missing
                val existingCount = questionRepository.getQuestionCountForSet(assetName)
                if (existingCount == 0) {
                    questionRepository.insertQuestions(questions, assetName)

                    val questionSet = QuestionSet(
                        sourceFileName = assetName,
                        title = response.meta.title,
                        version = response.meta.version,
                        lang = response.meta.lang,
                        importTimestamp = System.currentTimeMillis(),
                        questionCount = questions.size
                    )
                    questionRepository.insertQuestionSet(questionSet)
                    totalLoaded += questions.size
                }
            }

            Result.success(totalLoaded)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

