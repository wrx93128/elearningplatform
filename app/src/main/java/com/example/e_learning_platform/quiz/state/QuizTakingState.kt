package com.example.e_learning_platform.quiz.state

import com.example.e_learning_platform.quiz.model.Question
import com.example.e_learning_platform.quiz.model.QuestionAnswerSubmission
import com.example.e_learning_platform.quiz.model.QuestionType
import com.example.e_learning_platform.quiz.model.Quiz
import com.example.e_learning_platform.quiz.model.QuizSubmissionRequest

data class QuizTakingState(
    val quiz: Quiz,
    val currentQuestionIndex: Int = 0,
    val selectedOptions: Map<String, Set<String>> = emptyMap()
) {
    val currentQuestion: Question?
        get() = quiz.questions.getOrNull(currentQuestionIndex)

    val questionCount: Int
        get() = quiz.questions.size

    val hasNextQuestion: Boolean
        get() = currentQuestionIndex < questionCount - 1

    val hasPreviousQuestion: Boolean
        get() = currentQuestionIndex > 0

    val answeredCount: Int
        get() = quiz.questions.count { question ->
            val selections = selectedOptions[question.id]
            !selections.isNullOrEmpty()
        }

    val isAllAnswered: Boolean
        get() = answeredCount == questionCount

    fun isOptionSelected(questionId: String, optionId: String): Boolean {
        return selectedOptions[questionId]?.contains(optionId) == true
    }

    fun getSelectedOptions(questionId: String): Set<String> {
        return selectedOptions[questionId].orEmpty()
    }

    fun isQuestionAnswered(questionId: String): Boolean {
        return !selectedOptions[questionId].isNullOrEmpty()
    }

    fun selectOption(questionId: String, optionId: String, type: QuestionType): QuizTakingState {
        val currentSelections = selectedOptions[questionId].orEmpty()
        val updatedSelections = when (type) {
            QuestionType.SINGLE_CHOICE -> setOf(optionId)
            QuestionType.MULTIPLE_CHOICE -> {
                if (currentSelections.contains(optionId)) {
                    currentSelections - optionId
                } else {
                    currentSelections + optionId
                }
            }
        }
        return copy(
            selectedOptions = selectedOptions + (questionId to updatedSelections)
        )
    }

    fun clearSelection(questionId: String): QuizTakingState {
        return copy(selectedOptions = selectedOptions - questionId)
    }

    fun nextQuestion(): QuizTakingState {
        return if (hasNextQuestion) copy(currentQuestionIndex = currentQuestionIndex + 1) else this
    }

    fun previousQuestion(): QuizTakingState {
        return if (hasPreviousQuestion) copy(currentQuestionIndex = currentQuestionIndex - 1) else this
    }

    fun goToQuestion(index: Int): QuizTakingState {
        val safeIndex = index.coerceIn(0, (questionCount - 1).coerceAtLeast(0))
        return copy(currentQuestionIndex = safeIndex)
    }

    fun buildSubmissionRequest(): QuizSubmissionRequest {
        val answers = quiz.questions.map { question ->
            QuestionAnswerSubmission(
                questionId = question.id,
                selectedOptionIds = selectedOptions[question.id]?.toList().orEmpty()
            )
        }
        return QuizSubmissionRequest(
            quizId = quiz.id,
            answers = answers
        )
    }
}
