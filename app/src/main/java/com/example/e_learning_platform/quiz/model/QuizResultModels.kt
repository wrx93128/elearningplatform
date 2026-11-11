package com.example.e_learning_platform.quiz.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionResult(
    @SerialName("questionId")
    val questionId: String,
    @SerialName("isCorrect")
    val isCorrect: Boolean,
    @SerialName("scoreAwarded")
    val scoreAwarded: Int,
    @SerialName("maxScore")
    val maxScore: Int,
    @SerialName("userSelectedOptionIds")
    val userSelectedOptionIds: List<String>,
    @SerialName("correctOptionIds")
    val correctOptionIds: List<String>,
    @SerialName("explanation")
    val explanation: String? = null
)

@Serializable
data class QuizResult(
    @SerialName("submissionId")
    val submissionId: String,
    @SerialName("quizId")
    val quizId: String,
    @SerialName("quizTitle")
    val quizTitle: String,
    @SerialName("score")
    val score: Int,
    @SerialName("maxScore")
    val maxScore: Int,
    @SerialName("percentage")
    val percentage: Double,
    @SerialName("passed")
    val passed: Boolean,
    @SerialName("questionResults")
    val questionResults: List<QuestionResult>
)
