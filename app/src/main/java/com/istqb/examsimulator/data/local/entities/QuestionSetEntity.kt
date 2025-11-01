package com.istqb.examsimulator.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_sets")
data class QuestionSetEntity(
    @PrimaryKey
    val sourceFileName: String,
    val title: String,
    val version: String,
    val lang: String,
    val importTimestamp: Long,
    val questionCount: Int
)

