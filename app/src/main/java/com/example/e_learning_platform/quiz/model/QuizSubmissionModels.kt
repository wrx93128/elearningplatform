package com.example.e_learning_platform.quiz.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionAnswerSubmission(
    @SerialName("questionId")
    val questionId: String,
    @SerialName("selectedOptionIds")
    val selectedOptionIds: List<String>
)

@Serializable
data class QuizSubmissionRequest(
    @SerialName("quizId")
    val quizId: String,
    @SerialName("answers")
    val answers: List<QuestionAnswerSubmission>
)
