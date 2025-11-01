package com.istqb.examsimulator.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.istqb.examsimulator.data.local.converters.StringListConverter

@Entity(
    tableName = "attempt_answers",
    foreignKeys = [
        ForeignKey(
            entity = ExamAttemptEntity::class,
            parentColumns = ["attemptId"],
            childColumns = ["attemptId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["attemptId"]),
        Index(value = ["questionId"])
    ]
)
@TypeConverters(StringListConverter::class)
data class AttemptAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val answerId: Long = 0,
    val attemptId: String,
    val questionId: Int,
    val selectedOptions: List<String>,
    val isCorrect: Boolean,
    val isFlagged: Boolean
)

