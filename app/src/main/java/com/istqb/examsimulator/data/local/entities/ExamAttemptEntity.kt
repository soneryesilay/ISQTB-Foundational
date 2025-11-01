package com.istqb.examsimulator.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.istqb.examsimulator.data.local.converters.StringListConverter

@Entity(tableName = "exam_attempts")
@TypeConverters(StringListConverter::class)
data class ExamAttemptEntity(
    @PrimaryKey
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

