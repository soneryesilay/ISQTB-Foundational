package com.istqb.examsimulator.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.istqb.examsimulator.data.local.converters.StringListConverter

@Entity(tableName = "questions")
@TypeConverters(StringListConverter::class)
data class QuestionEntity(
    @PrimaryKey
    val id: Int,
    val type: String,
    val text: String,
    val lo: String?,
    val kLevel: String?,
    val score: Int,
    val image: String?,
    val setSource: String,
    val answer: List<String> // Store correct answers
)

