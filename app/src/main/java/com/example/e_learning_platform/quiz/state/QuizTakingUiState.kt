package com.example.e_learning_platform.quiz.state

import com.example.e_learning_platform.quiz.model.QuizResult

sealed interface QuizTakingUiState {
    object Loading : QuizTakingUiState

    data class InProgress(
        val takingState: QuizTakingState,
        val timeRemainingSeconds: Int? = null,
        val isSubmitDialogOpen: Boolean = false
    ) : QuizTakingUiState

    object Submitting : QuizTakingUiState

    data class Completed(
        val result: QuizResult
    ) : QuizTakingUiState

    data class Error(
        val message: String
    ) : QuizTakingUiState
}
