package com.istqb.examsimulator.data.model

data class Question(
    val id: Int,
    val type: String = "single",
    val text: String,
    val options: Map<String, String>,
    val answer: List<String>,
    val lo: String? = null,
    val kLevel: String? = null,
    val score: Int = 1,
    val image: String? = null,
    val setSource: String? = null
)

