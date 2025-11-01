package com.istqb.examsimulator.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.istqb.examsimulator.data.local.entities.ExamAttemptEntity
import com.istqb.examsimulator.data.local.entities.AttemptAnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: ExamAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<AttemptAnswerEntity>)

    @Query("SELECT * FROM exam_attempts ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentAttempts(limit: Int = 5): Flow<List<ExamAttemptEntity>>

    @Query("SELECT * FROM exam_attempts ORDER BY createdAt DESC")
    fun getAllAttempts(): Flow<List<ExamAttemptEntity>>

    @Transaction
    @Query("SELECT * FROM exam_attempts WHERE attemptId = :attemptId")
    suspend fun getAttemptWithAnswers(attemptId: String): AttemptWithAnswers?

    @Query("SELECT AVG(successPercent) FROM exam_attempts")
    fun getAverageSuccessPercent(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM exam_attempts")
    fun getTotalAttemptCount(): Flow<Int>
}

data class AttemptWithAnswers(
    val attempt: ExamAttemptEntity,
    val answers: List<AttemptAnswerEntity>
)

