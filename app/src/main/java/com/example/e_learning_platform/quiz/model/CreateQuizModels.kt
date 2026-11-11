package com.example.e_learning_platform.quiz.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CreateOptionRequest(
    @SerialName("id")
    val id: String = UUID.randomUUID().toString(),
    @SerialName("text")
    val text: String,
    @SerialName("isCorrect")
    val isCorrect: Boolean = false
)

@Serializable
data class CreateQuestionRequest(
    @SerialName("id")
    val id: String = UUID.randomUUID().toString(),
    @SerialName("text")
    val text: String,
    @SerialName("type")
    val type: QuestionType,
    @SerialName("options")
    val options: List<CreateOptionRequest>,
    @SerialName("points")
    val points: Int = 1,
    @SerialName("explanation")
    val explanation: String? = null
)

@Serializable
data class CreateQuizRequest(
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String = "",
    @SerialName("questions")
    val questions: List<CreateQuestionRequest>,
    @SerialName("timeLimitMinutes")
    val timeLimitMinutes: Int? = null,
    @SerialName("passingPercentage")
    val passingPercentage: Int = 60
)
