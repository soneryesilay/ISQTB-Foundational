package com.istqb.examsimulator.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.istqb.examsimulator.data.local.entities.QuestionSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionSetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: QuestionSetEntity)

    @Query("SELECT * FROM question_sets ORDER BY importTimestamp DESC")
    fun getAllSets(): Flow<List<QuestionSetEntity>>

    @Query("SELECT * FROM question_sets WHERE sourceFileName = :fileName")
    suspend fun getSetByFileName(fileName: String): QuestionSetEntity?
}

