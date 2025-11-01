package com.istqb.examsimulator.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "options",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["questionId"])]
)
data class OptionEntity(
    @PrimaryKey(autoGenerate = true)
    val optionId: Long = 0,
    val questionId: Int,
    val key: String, // a, b, c, d, e
    val text: String
)

