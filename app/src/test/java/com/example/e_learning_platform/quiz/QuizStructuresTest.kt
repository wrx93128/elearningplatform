package com.example.e_learning_platform.quiz

import com.example.e_learning_platform.quiz.api.MockQuizApiService
import com.example.e_learning_platform.quiz.model.CreateOptionRequest
import com.example.e_learning_platform.quiz.model.CreateQuestionRequest
import com.example.e_learning_platform.quiz.model.CreateQuizRequest
import com.example.e_learning_platform.quiz.model.Question
import com.example.e_learning_platform.quiz.model.QuestionType
import com.example.e_learning_platform.quiz.model.Quiz
import com.example.e_learning_platform.quiz.model.QuizOption
import com.example.e_learning_platform.quiz.model.QuizValidationResult
import com.example.e_learning_platform.quiz.model.QuizValidator
import com.example.e_learning_platform.quiz.state.QuizCreationState
import com.example.e_learning_platform.quiz.state.QuizTakingState
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class QuizStructuresTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun serialization_createQuizRequestRoundTrip() {
        val request = CreateQuizRequest(
            title = "Math 101",
            description = "Basic Arithmetic",
            questions = listOf(
                CreateQuestionRequest(
                    id = "q1",
                    text = "What is 2 + 2?",
                    type = QuestionType.SINGLE_CHOICE,
                    points = 2,
                    options = listOf(
                        CreateOptionRequest(id = "o1", text = "3", isCorrect = false),
                        CreateOptionRequest(id = "o2", text = "4", isCorrect = true)
                    )
                )
            )
        )

        val jsonString = json.encodeToString(request)
        assertTrue(jsonString.contains("\"SINGLE_CHOICE\""))
        assertTrue(jsonString.contains("\"isCorrect\": true"))

        val decoded = json.decodeFromString<CreateQuizRequest>(jsonString)
        assertEquals(request.title, decoded.title)
        assertEquals(request.questions.size, decoded.questions.size)
        assertEquals(request.questions.first().type, decoded.questions.first().type)
        assertTrue(decoded.questions.first().options[1].isCorrect)
    }

    @Test
    fun serialization_quizPublicModelDoesNotIncludeIsCorrect() {
        val quiz = Quiz(
            id = "quiz-123",
            title = "History Quiz",
            questions = listOf(
                Question(
                    id = "q1",
                    text = "Who discovered America?",
                    type = QuestionType.SINGLE_CHOICE,
                    options = listOf(
                        QuizOption(id = "o1", text = "Christopher Columbus"),
                        QuizOption(id = "o2", text = "Isaac Newton")
                    )
                )
            )
        )

        val jsonString = json.encodeToString(quiz)
        assertFalse(jsonString.contains("isCorrect"))

        val decoded = json.decodeFromString<Quiz>(jsonString)
        assertEquals("quiz-123", decoded.id)
        assertEquals(2, decoded.questions.first().options.size)
    }

    @Test
    fun validation_singleChoiceRequiresExactlyOneCorrectOption() {
        val invalidZeroCorrect = CreateQuizRequest(
            title = "Test Quiz",
            questions = listOf(
                CreateQuestionRequest(
                    text = "Sample Question",
                    type = QuestionType.SINGLE_CHOICE,
                    options = listOf(
                        CreateOptionRequest(text = "Option A", isCorrect = false),
                        CreateOptionRequest(text = "Option B", isCorrect = false)
                    )
                )
            )
        )
        val resultZero = QuizValidator.validateCreateQuiz(invalidZeroCorrect)
        assertTrue(resultZero is QuizValidationResult.Invalid)

        val invalidMultipleCorrect = CreateQuizRequest(
            title = "Test Quiz",
            questions = listOf(
                CreateQuestionRequest(
                    text = "Sample Question",
                    type = QuestionType.SINGLE_CHOICE,
                    options = listOf(
                        CreateOptionRequest(text = "Option A", isCorrect = true),
                        CreateOptionRequest(text = "Option B", isCorrect = true)
                    )
                )
            )
        )
        val resultMultiple = QuizValidator.validateCreateQuiz(invalidMultipleCorrect)
        assertTrue(resultMultiple is QuizValidationResult.Invalid)

        val validSingle = CreateQuizRequest(
            title = "Test Quiz",
            questions = listOf(
                CreateQuestionRequest(
                    text = "Sample Question",
                    type = QuestionType.SINGLE_CHOICE,
                    options = listOf(
                        CreateOptionRequest(text = "Option A", isCorrect = true),
                        CreateOptionRequest(text = "Option B", isCorrect = false)
                    )
                )
            )
        )
        val resultValid = QuizValidator.validateCreateQuiz(validSingle)
        assertTrue(resultValid is QuizValidationResult.Valid)
    }

    @Test
    fun validation_multipleChoiceRequiresAtLeastOneCorrectOption() {
        val invalidMultipleChoice = CreateQuizRequest(
            title = "Multi Quiz",
            questions = listOf(
                CreateQuestionRequest(
                    text = "Select primes",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf(
                        CreateOptionRequest(text = "4", isCorrect = false),
                        CreateOptionRequest(text = "6", isCorrect = false)
                    )
                )
            )
        )
        val resultInvalid = QuizValidator.validateCreateQuiz(invalidMultipleChoice)
        assertTrue(resultInvalid is QuizValidationResult.Invalid)

        val validMultipleChoice = CreateQuizRequest(
            title = "Multi Quiz",
            questions = listOf(
                CreateQuestionRequest(
                    text = "Select primes",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf(
                        CreateOptionRequest(text = "2", isCorrect = true),
                        CreateOptionRequest(text = "3", isCorrect = true),
                        CreateOptionRequest(text = "4", isCorrect = false)
                    )
                )
            )
        )
        val resultValid = QuizValidator.validateCreateQuiz(validMultipleChoice)
        assertTrue(resultValid is QuizValidationResult.Valid)
    }

    @Test
    fun quizTakingState_selectionAndSubmissionFlow() {
        val quiz = Quiz(
            id = "quiz-1",
            title = "Sample Quiz",
            questions = listOf(
                Question(
                    id = "q1",
                    text = "Single choice question",
                    type = QuestionType.SINGLE_CHOICE,
                    options = listOf(QuizOption("o1", "A"), QuizOption("o2", "B"))
                ),
                Question(
                    id = "q2",
                    text = "Multiple choice question",
                    type = QuestionType.MULTIPLE_CHOICE,
                    options = listOf(QuizOption("o3", "C"), QuizOption("o4", "D"), QuizOption("o5", "E"))
                )
            )
        )

        var state = QuizTakingState(quiz = quiz)
        assertEquals(0, state.answeredCount)
        assertFalse(state.isAllAnswered)

        // Select single choice option o1
        state = state.selectOption("q1", "o1", QuestionType.SINGLE_CHOICE)
        assertTrue(state.isOptionSelected("q1", "o1"))
        assertEquals(setOf("o1"), state.getSelectedOptions("q1"))

        // Switch single choice to o2
        state = state.selectOption("q1", "o2", QuestionType.SINGLE_CHOICE)
        assertFalse(state.isOptionSelected("q1", "o1"))
        assertTrue(state.isOptionSelected("q1", "o2"))

        // Select multiple choice options o3 and o4
        state = state.selectOption("q2", "o3", QuestionType.MULTIPLE_CHOICE)
        state = state.selectOption("q2", "o4", QuestionType.MULTIPLE_CHOICE)
        assertEquals(setOf("o3", "o4"), state.getSelectedOptions("q2"))

        // Toggle o3 off
        state = state.selectOption("q2", "o3", QuestionType.MULTIPLE_CHOICE)
        assertEquals(setOf("o4"), state.getSelectedOptions("q2"))

        // Toggle o3 back on
        state = state.selectOption("q2", "o3", QuestionType.MULTIPLE_CHOICE)
        assertEquals(setOf("o4", "o3"), state.getSelectedOptions("q2"))

        assertEquals(2, state.answeredCount)
        assertTrue(state.isAllAnswered)

        val submission = state.buildSubmissionRequest()
        assertEquals("quiz-1", submission.quizId)
        assertEquals(2, submission.answers.size)
        assertEquals(listOf("o2"), submission.answers.first { it.questionId == "q1" }.selectedOptionIds)
    }

    @Test
    fun quizCreationState_toggleCorrectnessSingleChoiceResetsOthers() {
        var state = QuizCreationState(title = "Builder Test")
        val qId = state.questions.first().id
        val opt1Id = state.questions.first().options[0].id
        val opt2Id = state.questions.first().options[1].id

        // Initially opt1 is correct
        assertTrue(state.questions.first().options.first { it.id == opt1Id }.isCorrect)

        // Toggle opt2 as correct in single choice -> opt1 should automatically become false
        state = state.toggleOptionCorrectness(qId, opt2Id)
        assertFalse(state.questions.first().options.first { it.id == opt1Id }.isCorrect)
        assertTrue(state.questions.first().options.first { it.id == opt2Id }.isCorrect)
    }

    @Test
    fun mockApiService_endToEndCreateAndSubmitFlow() = runBlocking {
        val api = MockQuizApiService()

        // 1. Get existing sample quizzes
        val initialQuizzes = api.getQuizzes().getOrThrow()
        assertEquals(1, initialQuizzes.size)

        // 2. Create a new quiz
        val createRequest = CreateQuizRequest(
            title = "Science 101",
            description = "Test science facts",
            questions = listOf(
                CreateQuestionRequest(
                    id = "q1",
                    text = "What is the chemical formula for water?",
                    type = QuestionType.SINGLE_CHOICE,
                    points = 5,
                    options = listOf(
                        CreateOptionRequest(id = "o1", text = "CO2", isCorrect = false),
                        CreateOptionRequest(id = "o2", text = "H2O", isCorrect = true)
                    )
                ),
                CreateQuestionRequest(
                    id = "q2",
                    text = "Select all noble gases",
                    type = QuestionType.MULTIPLE_CHOICE,
                    points = 5,
                    options = listOf(
                        CreateOptionRequest(id = "o3", text = "Helium", isCorrect = true),
                        CreateOptionRequest(id = "o4", text = "Oxygen", isCorrect = false),
                        CreateOptionRequest(id = "o5", text = "Neon", isCorrect = true)
                    )
                )
            ),
            passingPercentage = 50
        )

        val createdQuiz = api.createQuiz(createRequest).getOrThrow()
        assertEquals("Science 101", createdQuiz.title)
        assertEquals(10, createdQuiz.totalPoints)

        // 3. Take the quiz and submit answers
        var takingState = QuizTakingState(quiz = createdQuiz)
        takingState = takingState.selectOption("q1", "o2", QuestionType.SINGLE_CHOICE) // correct
        takingState = takingState.selectOption("q2", "o3", QuestionType.MULTIPLE_CHOICE) // correct
        takingState = takingState.selectOption("q2", "o5", QuestionType.MULTIPLE_CHOICE) // correct

        val submission = takingState.buildSubmissionRequest()
        val result = api.submitQuiz(submission).getOrThrow()

        // 4. Verify results
        assertEquals(10, result.score)
        assertEquals(10, result.maxScore)
        assertEquals(100.0, result.percentage, 0.01)
        assertTrue(result.passed)
        assertEquals(2, result.questionResults.size)
        assertTrue(result.questionResults.all { it.isCorrect })
    }

    @Test
    fun quizCreationState_fullBuilderAndApiPersistence() = runBlocking {
        var state = QuizCreationState()
        state = state.updateTitle("Nowy quiz testowy")
        state = state.updateDescription("Opis quizu testowego")
        state = state.updateTimeLimit(15)
        state = state.updatePassingPercentage(75)

        val q1Id = state.questions.first().id
        state = state.updateQuestionText(q1Id, "Ile wynosi 10 * 10?")
        state = state.updateQuestionPoints(q1Id, 4)
        state = state.updateQuestionExplanation(q1Id, "10 * 10 = 100")

        val q1Opt1Id = state.questions.first().options[0].id
        val q1Opt2Id = state.questions.first().options[1].id
        state = state.updateOptionText(q1Id, q1Opt1Id, "100")
        state = state.updateOptionText(q1Id, q1Opt2Id, "90")

        val validation = state.validate()
        assertTrue(validation is QuizValidationResult.Valid)

        val createRequest = state.toCreateQuizRequest()
        val createdQuiz = MockQuizApiService.default.createQuiz(createRequest).getOrThrow()
        assertEquals("Nowy quiz testowy", createdQuiz.title)
        assertEquals(4, createdQuiz.totalPoints)
        assertEquals(1, createdQuiz.questionCount)

        val fetched = MockQuizApiService.default.getQuizById(createdQuiz.id).getOrThrow()
        assertEquals(createdQuiz.id, fetched.id)
    }
}
