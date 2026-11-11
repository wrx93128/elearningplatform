package com.example.e_learning_platform.quiz.model

sealed interface QuizValidationResult {
    data object Valid : QuizValidationResult
    data class Invalid(val errors: List<String>) : QuizValidationResult {
        val errorMessage: String get() = errors.joinToString("\n")
    }
}

object QuizValidator {

    /**
     * Validates whether a [CreateQuizRequest] meets all integrity rules:
     * - Title must not be blank
     * - Must contain at least 1 question
     * - Each question must have non-blank text
     * - Each question must have at least 2 options with non-blank text
     * - Single choice questions must have exactly 1 correct option
     * - Multiple choice questions must have at least 1 correct option
     */
    fun validateCreateQuiz(request: CreateQuizRequest): QuizValidationResult {
        val errors = mutableListOf<String>()

        if (request.title.isBlank()) {
            errors.add("Quiz title cannot be blank.")
        }
        if (request.questions.isEmpty()) {
            errors.add("Quiz must have at least one question.")
        }

        request.questions.forEachIndexed { qIdx, question ->
            val qNum = qIdx + 1
            if (question.text.isBlank()) {
                errors.add("Question #$qNum text cannot be blank.")
            }
            if (question.options.size < 2) {
                errors.add("Question #$qNum must have at least 2 options.")
            }
            question.options.forEachIndexed { oIdx, option ->
                if (option.text.isBlank()) {
                    errors.add("Question #$qNum option #${oIdx + 1} text cannot be blank.")
                }
            }
            val correctCount = question.options.count { it.isCorrect }
            when (question.type) {
                QuestionType.SINGLE_CHOICE -> {
                    if (correctCount != 1) {
                        errors.add("Question #$qNum is single choice and must have exactly 1 correct answer (currently $correctCount).")
                    }
                }
                QuestionType.MULTIPLE_CHOICE -> {
                    if (correctCount < 1) {
                        errors.add("Question #$qNum is multiple choice and must have at least 1 correct answer.")
                    }
                }
            }
        }

        return if (errors.isEmpty()) QuizValidationResult.Valid else QuizValidationResult.Invalid(errors)
    }

    fun validateSubmission(quiz: Quiz, submission: QuizSubmissionRequest): QuizValidationResult {
        val errors = mutableListOf<String>()

        if (submission.quizId != quiz.id) {
            errors.add("Submission quiz ID (${submission.quizId}) does not match current quiz ID (${quiz.id}).")
        }

        val questionIdsInQuiz = quiz.questions.map { it.id }.toSet()
        val submittedQuestionIds = submission.answers.map { it.questionId }.toSet()

        val unknownIds = submittedQuestionIds - questionIdsInQuiz
        if (unknownIds.isNotEmpty()) {
            errors.add("Submission contains unknown question IDs: $unknownIds")
        }

        return if (errors.isEmpty()) QuizValidationResult.Valid else QuizValidationResult.Invalid(errors)
    }
}
