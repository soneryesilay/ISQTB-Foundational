package com.istqb.examsimulator.data.model

data class QuestionSet(
    val sourceFileName: String,
    val title: String,
    val version: String,
    val lang: String,
    val importTimestamp: Long,
    val questionCount: Int
) {
    // Use sourceFileName as unique ID
    val id: String get() = sourceFileName
}

