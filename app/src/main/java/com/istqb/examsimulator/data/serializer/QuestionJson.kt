package com.istqb.examsimulator.data.serializer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionJsonResponse(
    val meta: MetaJson,
    val questions: List<QuestionJson>
)

@Serializable
data class MetaJson(
    val title: String,
    val version: String,
    val lang: String,
    val source: String
)

@Serializable
data class QuestionJson(
    val id: Int,
    val type: String = "single",
    val score: Int = 1,
    val lo: String? = null,
    @SerialName("kLevel") val kLevel: String? = null,
    val text: String,
    val options: Map<String, String>,
    val answer: List<String>,
    val image: String? = null,
    val note: String? = null
)

