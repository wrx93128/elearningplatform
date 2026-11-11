package com.example.e_learning_platform.quiz.api

import com.example.e_learning_platform.quiz.model.CreateOptionRequest
import com.example.e_learning_platform.quiz.model.CreateQuestionRequest
import com.example.e_learning_platform.quiz.model.CreateQuizRequest
import com.example.e_learning_platform.quiz.model.Question
import com.example.e_learning_platform.quiz.model.QuestionResult
import com.example.e_learning_platform.quiz.model.QuestionType
import com.example.e_learning_platform.quiz.model.Quiz
import com.example.e_learning_platform.quiz.model.QuizOption
import com.example.e_learning_platform.quiz.model.QuizResult
import com.example.e_learning_platform.quiz.model.QuizSubmissionRequest
import com.example.e_learning_platform.quiz.model.QuizSummary
import com.example.e_learning_platform.quiz.model.QuizValidationResult
import com.example.e_learning_platform.quiz.model.QuizValidator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class MockQuizApiService : QuizApiService {

    private data class StoredQuiz(
        val id: String,
        val createRequest: CreateQuizRequest
    )

    private val mutex = Mutex()
    private val storedQuizzes = mutableMapOf<String, StoredQuiz>()

    init {
        val sampleQuizId = "sample-quiz-1"
        storedQuizzes[sampleQuizId] = StoredQuiz(
            id = sampleQuizId,
            createRequest = CreateQuizRequest(
                title = "Kotlin & Android Basics",
                description = "Test your fundamental knowledge of Kotlin and Android development.",
                timeLimitMinutes = 10,
                passingPercentage = 60,
                questions = listOf(
                    CreateQuestionRequest(
                        id = "q1",
                        text = "Which keyword in Kotlin declares a read-only variable?",
                        type = QuestionType.SINGLE_CHOICE,
                        points = 2,
                        explanation = "'val' is used for immutable/read-only variables, whereas 'var' is mutable.",
                        options = listOf(
                            CreateOptionRequest(id = "q1_o1", text = "var", isCorrect = false),
                            CreateOptionRequest(id = "q1_o2", text = "val", isCorrect = true),
                            CreateOptionRequest(id = "q1_o3", text = "const", isCorrect = false),
                            CreateOptionRequest(id = "q1_o4", text = "let", isCorrect = false)
                        )
                    ),
                    CreateQuestionRequest(
                        id = "q2",
                        text = "Which of the following are Jetpack Compose layout composables? (Select all that apply)",
                        type = QuestionType.MULTIPLE_CHOICE,
                        points = 3,
                        explanation = "Column, Row, and Box are standard Compose layout primitives.",
                        options = listOf(
                            CreateOptionRequest(id = "q2_o1", text = "Column", isCorrect = true),
                            CreateOptionRequest(id = "q2_o2", text = "LinearLayout", isCorrect = false),
                            CreateOptionRequest(id = "q2_o3", text = "Row", isCorrect = true),
                            CreateOptionRequest(id = "q2_o4", text = "Box", isCorrect = true)
                        )
                    )
                )
            )
        )
    }

    override suspend fun createQuiz(request: CreateQuizRequest): Result<Quiz> = mutex.withLock {
        when (val validation = QuizValidator.validateCreateQuiz(request)) {
            is QuizValidationResult.Invalid -> {
                return@withLock Result.failure(IllegalArgumentException(validation.errorMessage))
            }
            is QuizValidationResult.Valid -> {
                val newId = UUID.randomUUID().toString()
                val stored = StoredQuiz(id = newId, createRequest = request)
                storedQuizzes[newId] = stored
                Result.success(toPublicQuiz(stored))
            }
        }
    }

    override suspend fun getQuizzes(): Result<List<QuizSummary>> = mutex.withLock {
        val summaries = storedQuizzes.values.map { stored ->
            val req = stored.createRequest
            QuizSummary(
                id = stored.id,
                title = req.title,
                description = req.description,
                questionCount = req.questions.size,
                totalPoints = req.questions.sumOf { it.points },
                timeLimitMinutes = req.timeLimitMinutes
            )
        }
        Result.success(summaries)
    }

    override suspend fun getQuizById(id: String): Result<Quiz> = mutex.withLock {
        val stored = storedQuizzes[id]
            ?: return@withLock Result.failure(NoSuchElementException("Quiz with id '$id' not found"))
        Result.success(toPublicQuiz(stored))
    }

    override suspend fun submitQuiz(submission: QuizSubmissionRequest): Result<QuizResult> = mutex.withLock {
        val stored = storedQuizzes[submission.quizId]
            ?: return@withLock Result.failure(NoSuchElementException("Quiz with id '${submission.quizId}' not found"))

        val submissionAnswersMap = submission.answers.associateBy { it.questionId }
        val questionResults = mutableListOf<QuestionResult>()

        var totalScoreAwarded = 0
        var totalMaxScore = 0

        stored.createRequest.questions.forEach { question ->
            val maxPoints = question.points
            totalMaxScore += maxPoints

            val userSelection = submissionAnswersMap[question.id]?.selectedOptionIds.orEmpty().toSet()
            val correctOptionIds = question.options.filter { it.isCorrect }.map { it.id }.toSet()

            val isCorrect = when (question.type) {
                QuestionType.SINGLE_CHOICE -> {
                    userSelection.size == 1 && userSelection.first() == correctOptionIds.firstOrNull()
                }
                QuestionType.MULTIPLE_CHOICE -> {
                    userSelection.isNotEmpty() && userSelection == correctOptionIds
                }
            }

            val scoreAwarded = if (isCorrect) maxPoints else 0
            totalScoreAwarded += scoreAwarded

            questionResults.add(
                QuestionResult(
                    questionId = question.id,
                    isCorrect = isCorrect,
                    scoreAwarded = scoreAwarded,
                    maxScore = maxPoints,
                    userSelectedOptionIds = userSelection.toList(),
                    correctOptionIds = correctOptionIds.toList(),
                    explanation = question.explanation
                )
            )
        }

        val percentage = if (totalMaxScore > 0) {
            (totalScoreAwarded.toDouble() / totalMaxScore) * 100.0
        } else {
            0.0
        }
        val passed = percentage >= stored.createRequest.passingPercentage

        val result = QuizResult(
            submissionId = UUID.randomUUID().toString(),
            quizId = stored.id,
            quizTitle = stored.createRequest.title,
            score = totalScoreAwarded,
            maxScore = totalMaxScore,
            percentage = percentage,
            passed = passed,
            questionResults = questionResults
        )

        Result.success(result)
    }

    private fun toPublicQuiz(stored: StoredQuiz): Quiz {
        return Quiz(
            id = stored.id,
            title = stored.createRequest.title,
            description = stored.createRequest.description,
            timeLimitMinutes = stored.createRequest.timeLimitMinutes,
            passingPercentage = stored.createRequest.passingPercentage,
            questions = stored.createRequest.questions.map { q ->
                Question(
                    id = q.id,
                    text = q.text,
                    type = q.type,
                    points = q.points,
                    options = q.options.map { opt ->
                        QuizOption(id = opt.id, text = opt.text)
                    }
                )
            }
        )
    }
}
