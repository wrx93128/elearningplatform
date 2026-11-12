package com.example.e_learning_platform.quiz.state

import com.example.e_learning_platform.quiz.model.CreateOptionRequest
import com.example.e_learning_platform.quiz.model.CreateQuestionRequest
import com.example.e_learning_platform.quiz.model.CreateQuizRequest
import com.example.e_learning_platform.quiz.model.QuestionType
import com.example.e_learning_platform.quiz.model.QuizValidationResult
import com.example.e_learning_platform.quiz.model.QuizValidator
import java.util.UUID

data class QuizCreationState(
    val title: String = "",
    val description: String = "",
    val timeLimitMinutes: Int? = null,
    val passingPercentage: Int = 60,
    val questions: List<CreateQuestionRequest> = listOf(
        createDefaultQuestion(1, QuestionType.SINGLE_CHOICE)
    )
) {
    fun updateTitle(newTitle: String): QuizCreationState = copy(title = newTitle)

    fun updateDescription(newDescription: String): QuizCreationState = copy(description = newDescription)

    fun updateTimeLimit(minutes: Int?): QuizCreationState = copy(timeLimitMinutes = minutes)

    fun updatePassingPercentage(percentage: Int): QuizCreationState = copy(passingPercentage = percentage)

    fun addQuestion(type: QuestionType = QuestionType.SINGLE_CHOICE): QuizCreationState {
        val newQuestion = createDefaultQuestion(questions.size + 1, type)
        return copy(questions = questions + newQuestion)
    }

    fun removeQuestion(questionId: String): QuizCreationState {
        return copy(questions = questions.filterNot { it.id == questionId })
    }

    fun updateQuestionText(questionId: String, text: String): QuizCreationState {
        return copy(
            questions = questions.map { q ->
                if (q.id == questionId) q.copy(text = text) else q
            }
        )
    }

    fun updateQuestionType(questionId: String, newType: QuestionType): QuizCreationState {
        return copy(
            questions = questions.map { q ->
                if (q.id == questionId) {
                    val adjustedOptions = if (newType == QuestionType.SINGLE_CHOICE) {
                        // Ensure only the first marked correct option stays marked correct
                        var foundFirstCorrect = false
                        q.options.map { opt ->
                            if (opt.isCorrect && !foundFirstCorrect) {
                                foundFirstCorrect = true
                                opt
                            } else {
                                opt.copy(isCorrect = false)
                            }
                        }
                    } else {
                        q.options
                    }
                    q.copy(type = newType, options = adjustedOptions)
                } else q
            }
        )
    }

    fun updateQuestionPoints(questionId: String, points: Int): QuizCreationState {
        return copy(
            questions = questions.map { q ->
                if (q.id == questionId) q.copy(points = points.coerceAtLeast(1)) else q
            }
        )
    }

    fun updateQuestionExplanation(questionId: String, explanation: String): QuizCreationState {
        return copy(
            questions = questions.map { q ->
                if (q.id == questionId) q.copy(explanation = explanation.ifBlank { null }) else q
            }
        )
    }

    fun addOption(questionId: String, defaultText: String = ""): QuizCreationState {
        return copy(
            questions = questions.map { q ->
                if (q.id == questionId) {
                    val newOption = CreateOptionRequest(
                        id = UUID.randomUUID().toString(),
                        text = defaultText,
                        isCorrect = false
                    )
                    q.copy(options = q.options + newOption)
                } else q
            }
        )
    }

    fun removeOption(questionId: String, optionId: String): QuizCreationState {
        return copy(
            questions = questions.map { q ->
                if (q.id == questionId) {
                    q.copy(options = q.options.filterNot { it.id == optionId })
                } else q
            }
        )
    }

    fun updateOptionText(questionId: String, optionId: String, text: String): QuizCreationState {
        return copy(
            questions = questions.map { q ->
                if (q.id == questionId) {
                    q.copy(options = q.options.map { opt ->
                        if (opt.id == optionId) opt.copy(text = text) else opt
                    })
                } else q
            }
        )
    }

    fun toggleOptionCorrectness(questionId: String, optionId: String): QuizCreationState {
        return copy(
            questions = questions.map { q ->
                if (q.id == questionId) {
                    val updatedOptions = when (q.type) {
                        QuestionType.SINGLE_CHOICE -> {
                            q.options.map { opt ->
                                opt.copy(isCorrect = (opt.id == optionId))
                            }
                        }
                        QuestionType.MULTIPLE_CHOICE -> {
                            q.options.map { opt ->
                                if (opt.id == optionId) opt.copy(isCorrect = !opt.isCorrect) else opt
                            }
                        }
                    }
                    q.copy(options = updatedOptions)
                } else q
            }
        )
    }

    fun validate(): QuizValidationResult = QuizValidator.validateCreateQuiz(toCreateQuizRequest())

    fun toCreateQuizRequest(): CreateQuizRequest {
        return CreateQuizRequest(
            title = title.trim(),
            description = description.trim(),
            questions = questions,
            timeLimitMinutes = timeLimitMinutes,
            passingPercentage = passingPercentage
        )
    }

    companion object {
        fun createDefaultQuestion(
            index: Int,
            type: QuestionType = QuestionType.SINGLE_CHOICE
        ): CreateQuestionRequest {
            return CreateQuestionRequest(
                id = UUID.randomUUID().toString(),
                text = "",
                type = type,
                options = listOf(
                    CreateOptionRequest(text = "", isCorrect = true),
                    CreateOptionRequest(text = "", isCorrect = false)
                ),
                points = 1
            )
        }
    }
}
