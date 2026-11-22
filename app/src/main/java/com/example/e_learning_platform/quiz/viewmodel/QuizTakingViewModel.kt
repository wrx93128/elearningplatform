package com.example.e_learning_platform.quiz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_learning_platform.quiz.api.MockQuizApiService
import com.example.e_learning_platform.quiz.api.QuizApiService
import com.example.e_learning_platform.quiz.model.QuestionType
import com.example.e_learning_platform.quiz.model.Quiz
import com.example.e_learning_platform.quiz.state.QuizTakingState
import com.example.e_learning_platform.quiz.state.QuizTakingUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizTakingViewModel(
    private val apiService: QuizApiService = MockQuizApiService.default
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizTakingUiState>(QuizTakingUiState.Loading)
    val uiState: StateFlow<QuizTakingUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var loadedQuiz: Quiz? = null

    fun loadQuiz(quizId: String) {
        viewModelScope.launch {
            _uiState.value = QuizTakingUiState.Loading
            timerJob?.cancel()

            val result = apiService.getQuizById(quizId)
            result.onSuccess { quiz ->
                loadedQuiz = quiz
                startQuizState(quiz)
            }.onFailure { error ->
                _uiState.value = QuizTakingUiState.Error(
                    error.message ?: "Błąd podczas ładowania quizu"
                )
            }
        }
    }

    private fun startQuizState(quiz: Quiz) {
        val initialTakingState = QuizTakingState(quiz = quiz)
        val initialTimeSeconds = quiz.timeLimitMinutes?.let { it * 60 }

        _uiState.value = QuizTakingUiState.InProgress(
            takingState = initialTakingState,
            timeRemainingSeconds = initialTimeSeconds,
            isSubmitDialogOpen = false
        )

        if (initialTimeSeconds != null && initialTimeSeconds > 0) {
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val currentState = _uiState.value
                if (currentState !is QuizTakingUiState.InProgress) break

                val currentRemaining = currentState.timeRemainingSeconds ?: break
                if (currentRemaining > 1) {
                    _uiState.value = currentState.copy(
                        timeRemainingSeconds = currentRemaining - 1
                    )
                } else {
                    _uiState.value = currentState.copy(
                        timeRemainingSeconds = 0,
                        isSubmitDialogOpen = false
                    )
                    submitQuizInternal(currentState.takingState)
                    break
                }
            }
        }
    }

    fun selectOption(questionId: String, optionId: String) {
        val currentState = _uiState.value as? QuizTakingUiState.InProgress ?: return
        val currentQuestion = currentState.takingState.quiz.questions.find { it.id == questionId } ?: return

        val updatedTakingState = currentState.takingState.selectOption(
            questionId = questionId,
            optionId = optionId,
            type = currentQuestion.type
        )

        _uiState.value = currentState.copy(takingState = updatedTakingState)
    }

    fun nextQuestion() {
        val currentState = _uiState.value as? QuizTakingUiState.InProgress ?: return
        _uiState.value = currentState.copy(
            takingState = currentState.takingState.nextQuestion()
        )
    }

    fun previousQuestion() {
        val currentState = _uiState.value as? QuizTakingUiState.InProgress ?: return
        _uiState.value = currentState.copy(
            takingState = currentState.takingState.previousQuestion()
        )
    }

    fun goToQuestion(index: Int) {
        val currentState = _uiState.value as? QuizTakingUiState.InProgress ?: return
        _uiState.value = currentState.copy(
            takingState = currentState.takingState.goToQuestion(index)
        )
    }

    fun showSubmitDialog() {
        val currentState = _uiState.value as? QuizTakingUiState.InProgress ?: return
        _uiState.value = currentState.copy(isSubmitDialogOpen = true)
    }

    fun hideSubmitDialog() {
        val currentState = _uiState.value as? QuizTakingUiState.InProgress ?: return
        _uiState.value = currentState.copy(isSubmitDialogOpen = false)
    }

    fun submitQuiz() {
        val currentState = _uiState.value as? QuizTakingUiState.InProgress ?: return
        _uiState.value = currentState.copy(isSubmitDialogOpen = false)
        submitQuizInternal(currentState.takingState)
    }

    private fun submitQuizInternal(takingState: QuizTakingState) {
        timerJob?.cancel()
        viewModelScope.launch {
            _uiState.value = QuizTakingUiState.Submitting
            val submissionRequest = takingState.buildSubmissionRequest()
            val result = apiService.submitQuiz(submissionRequest)

            result.onSuccess { quizResult ->
                _uiState.value = QuizTakingUiState.Completed(quizResult)
            }.onFailure { error ->
                _uiState.value = QuizTakingUiState.Error(
                    error.message ?: "Błąd podczas wysyłania odpowiedzi"
                )
            }
        }
    }

    fun restartQuiz() {
        val quiz = loadedQuiz ?: return
        timerJob?.cancel()
        startQuizState(quiz)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
