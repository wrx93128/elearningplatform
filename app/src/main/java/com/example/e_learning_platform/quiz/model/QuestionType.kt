package com.example.e_learning_platform.quiz.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class QuestionType {
    @SerialName("SINGLE_CHOICE")
    SINGLE_CHOICE,

    @SerialName("MULTIPLE_CHOICE")
    MULTIPLE_CHOICE
}
