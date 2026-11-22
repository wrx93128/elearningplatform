package com.example.e_learning_platform.quiz

import com.example.e_learning_platform.quiz.api.MockQuizApiService
import com.example.e_learning_platform.quiz.state.QuizTakingUiState
import com.example.e_learning_platform.quiz.viewmodel.QuizTakingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizTakingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var apiService: MockQuizApiService
    private lateinit var viewModel: QuizTakingViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        apiService = MockQuizApiService()
        viewModel = QuizTakingViewModel(apiService = apiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadQuiz_successInitializesInProgressState() = runTest {
        viewModel.loadQuiz("sample-quiz-1")
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state is QuizTakingUiState.InProgress)

        val inProgress = state as QuizTakingUiState.InProgress
        assertEquals("Kotlin & Android Basics", inProgress.takingState.quiz.title)
        assertEquals(2, inProgress.takingState.questionCount)
        assertEquals(0, inProgress.takingState.currentQuestionIndex)
        assertEquals(10 * 60, inProgress.timeRemainingSeconds)
    }

    @Test
    fun selectOption_updatesSelectedOptionsMap() = runTest {
        viewModel.loadQuiz("sample-quiz-1")
        testDispatcher.scheduler.runCurrent()

        // Select option 'q1_o2' for question 'q1' (Single Choice)
        viewModel.selectOption("q1", "q1_o2")

        var state = viewModel.uiState.value as QuizTakingUiState.InProgress
        assertTrue(state.takingState.isOptionSelected("q1", "q1_o2"))
        assertEquals(setOf("q1_o2"), state.takingState.getSelectedOptions("q1"))

        // Select option 'q2_o1' and 'q2_o3' for question 'q2' (Multiple Choice)
        viewModel.selectOption("q2", "q2_o1")
        viewModel.selectOption("q2", "q2_o3")

        state = viewModel.uiState.value as QuizTakingUiState.InProgress
        assertEquals(setOf("q2_o1", "q2_o3"), state.takingState.getSelectedOptions("q2"))
    }

    @Test
    fun navigation_nextPreviousAndGoToQuestion() = runTest {
        viewModel.loadQuiz("sample-quiz-1")
        testDispatcher.scheduler.runCurrent()

        var state = viewModel.uiState.value as QuizTakingUiState.InProgress
        assertEquals(0, state.takingState.currentQuestionIndex)

        // Go to next question
        viewModel.nextQuestion()
        state = viewModel.uiState.value as QuizTakingUiState.InProgress
        assertEquals(1, state.takingState.currentQuestionIndex)

        // Go back to previous question
        viewModel.previousQuestion()
        state = viewModel.uiState.value as QuizTakingUiState.InProgress
        assertEquals(0, state.takingState.currentQuestionIndex)

        // Jump to index 1
        viewModel.goToQuestion(1)
        state = viewModel.uiState.value as QuizTakingUiState.InProgress
        assertEquals(1, state.takingState.currentQuestionIndex)
    }

    @Test
    fun timer_decrementsTimeAndAutoSubmitsWhenExpired() = runTest {
        viewModel.loadQuiz("sample-quiz-1")
        testDispatcher.scheduler.runCurrent()

        var state = viewModel.uiState.value as QuizTakingUiState.InProgress
        assertEquals(600, state.timeRemainingSeconds)

        // Advance by 5 seconds (5 * 1000ms)
        testDispatcher.scheduler.advanceTimeBy(5001L)
        testDispatcher.scheduler.runCurrent()
        state = viewModel.uiState.value as QuizTakingUiState.InProgress
        assertEquals(595, state.timeRemainingSeconds)

        // Advance remaining time to expire timer
        testDispatcher.scheduler.advanceTimeBy(600_000L)
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value is QuizTakingUiState.Completed)
    }

    @Test
    fun submitQuiz_evaluatesAnswersAndTransitionsToCompletedState() = runTest {
        viewModel.loadQuiz("sample-quiz-1")
        testDispatcher.scheduler.runCurrent()

        // Answer q1 correctly: q1_o2 (val)
        viewModel.selectOption("q1", "q1_o2")

        // Answer q2 correctly: q2_o1 (Column), q2_o3 (Row), q2_o4 (Box)
        viewModel.selectOption("q2", "q2_o1")
        viewModel.selectOption("q2", "q2_o3")
        viewModel.selectOption("q2", "q2_o4")

        viewModel.submitQuiz()
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state is QuizTakingUiState.Completed)

        val completed = state as QuizTakingUiState.Completed
        assertEquals(5, completed.result.score)
        assertEquals(5, completed.result.maxScore)
        assertEquals(100.0, completed.result.percentage, 0.01)
        assertTrue(completed.result.passed)
    }

    @Test
    fun restartQuiz_resetsSelectionsAndState() = runTest {
        viewModel.loadQuiz("sample-quiz-1")
        testDispatcher.scheduler.runCurrent()

        viewModel.selectOption("q1", "q1_o1")
        viewModel.submitQuiz()
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value is QuizTakingUiState.Completed)

        viewModel.restartQuiz()
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value as QuizTakingUiState.InProgress
        assertEquals(0, state.takingState.answeredCount)
        assertFalse(state.takingState.isQuestionAnswered("q1"))
    }
}
