package com.istqb.examsimulator.data.repository

import com.istqb.examsimulator.data.local.database.QuestionDao
import com.istqb.examsimulator.data.local.database.QuestionSetDao
import com.istqb.examsimulator.data.local.entities.OptionEntity
import com.istqb.examsimulator.data.local.entities.QuestionEntity
import com.istqb.examsimulator.data.local.entities.QuestionSetEntity
import com.istqb.examsimulator.data.model.Question
import com.istqb.examsimulator.data.model.QuestionSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class QuestionRepository(
    private val questionDao: QuestionDao,
    private val questionSetDao: QuestionSetDao
) {
    // Expose DAO for ViewModels that need direct access
    val questionSetDao: QuestionSetDao = questionSetDao
    suspend fun insertQuestions(questions: List<Question>, setSource: String) {
        questions.forEach { question ->
            val questionEntity = QuestionEntity(
                id = question.id,
                type = question.type,
                text = question.text,
                lo = question.lo,
                kLevel = question.kLevel,
                score = question.score,
                image = question.image,
                setSource = setSource,
                answer = question.answer
            )

            val insertedId = questionDao.insertQuestion(questionEntity)
            if (insertedId > 0) {
                val options = question.options.map { (key, text) ->
                    OptionEntity(
                        questionId = question.id,
                        key = key,
                        text = text
                    )
                }
                questionDao.insertOptions(options)
            }
        }
    }

    suspend fun insertQuestionSet(set: QuestionSet) {
        val entity = QuestionSetEntity(
            sourceFileName = set.sourceFileName,
            title = set.title,
            version = set.version,
            lang = set.lang,
            importTimestamp = set.importTimestamp,
            questionCount = set.questionCount
        )
        questionSetDao.insertSet(entity)
    }

    suspend fun getQuestionsBySets(setSources: List<String>): List<Question> {
        val questionEntities = questionDao.getQuestionsBySets(setSources)
        return questionEntities.map { entity ->
            val options = questionDao.getOptionsForQuestion(entity.id)
            Question(
                id = entity.id,
                type = entity.type,
                text = entity.text,
                options = options.associate { it.key to it.text },
                answer = entity.answer, // Load from entity
                lo = entity.lo,
                kLevel = entity.kLevel,
                score = entity.score,
                image = entity.image,
                setSource = entity.setSource
            )
        }
    }

    fun getAllQuestionSets(): Flow<List<QuestionSet>> {
        return questionSetDao.getAllSets().map { entities ->
            entities.map { entity ->
                QuestionSet(
                    sourceFileName = entity.sourceFileName,
                    title = entity.title,
                    version = entity.version,
                    lang = entity.lang,
                    importTimestamp = entity.importTimestamp,
                    questionCount = entity.questionCount
                )
            }
        }
    }

    suspend fun getQuestionCountForSet(setSource: String): Int {
        return questionDao.getQuestionCountForSet(setSource)
    }

    suspend fun questionExists(questionId: Int, setSource: String): Boolean {
        return questionDao.getQuestionByIdAndSet(questionId, setSource) != null
    }
}

