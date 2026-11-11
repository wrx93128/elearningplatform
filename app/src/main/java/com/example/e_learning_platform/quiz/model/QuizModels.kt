package com.example.e_learning_platform.quiz.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizOption(
    @SerialName("id")
    val id: String,
    @SerialName("text")
    val text: String
)

@Serializable
data class Question(
    @SerialName("id")
    val id: String,
    @SerialName("text")
    val text: String,
    @SerialName("type")
    val type: QuestionType,
    @SerialName("options")
    val options: List<QuizOption>,
    @SerialName("points")
    val points: Int = 1
)

@Serializable
data class Quiz(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String = "",
    @SerialName("questions")
    val questions: List<Question>,
    @SerialName("timeLimitMinutes")
    val timeLimitMinutes: Int? = null,
    @SerialName("passingPercentage")
    val passingPercentage: Int = 60
) {
    val totalPoints: Int
        get() = questions.sumOf { it.points }

    val questionCount: Int
        get() = questions.size
}

@Serializable
data class QuizSummary(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String = "",
    @SerialName("questionCount")
    val questionCount: Int,
    @SerialName("totalPoints")
    val totalPoints: Int,
    @SerialName("timeLimitMinutes")
    val timeLimitMinutes: Int? = null
)
