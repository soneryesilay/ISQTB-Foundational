package com.istqb.examsimulator.data.local.database

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.istqb.examsimulator.data.local.entities.QuestionEntity
import com.istqb.examsimulator.data.local.entities.OptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptions(options: List<OptionEntity>)

    @Query("SELECT * FROM questions WHERE setSource IN (:setSources)")
    suspend fun getQuestionsBySets(setSources: List<String>): List<QuestionEntity>

    @Query("SELECT * FROM options WHERE questionId = :questionId AND setSource = :setSource ORDER BY key ASC")
    suspend fun getOptionsForQuestion(questionId: Int, setSource: String): List<OptionEntity>

    @Transaction
    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionWithOptions(questionId: Int): QuestionWithOptions?

    @Query("SELECT * FROM questions WHERE setSource IN (:setSources)")
    fun observeQuestionsBySets(setSources: List<String>): Flow<List<QuestionEntity>>

    @Query("SELECT COUNT(*) FROM questions WHERE setSource = :setSource")
    suspend fun getQuestionCountForSet(setSource: String): Int

    @Query("SELECT * FROM questions WHERE id = :questionId AND setSource = :setSource")
    suspend fun getQuestionByIdAndSet(questionId: Int, setSource: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE setSource = :setSource")
    fun getQuestionsBySet(setSource: String): Flow<List<QuestionEntity>>

    @Query("DELETE FROM questions WHERE id = :questionId")
    suspend fun deleteQuestion(questionId: String)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Query("DELETE FROM options WHERE questionId = :questionId AND setSource = :setSource")
    suspend fun deleteOptionsForQuestion(questionId: Int, setSource: String)
}

data class QuestionWithOptions(
    @Embedded val question: QuestionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "questionId"
    )
    val options: List<OptionEntity>
)

