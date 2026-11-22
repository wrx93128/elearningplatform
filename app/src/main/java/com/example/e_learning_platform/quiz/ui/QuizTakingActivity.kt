package com.example.e_learning_platform.quiz.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.e_learning_platform.quiz.viewmodel.QuizTakingViewModel
import com.example.e_learning_platform.ui.theme.ElearningplatformTheme

class QuizTakingActivity : ComponentActivity() {

    companion object {
        const val EXTRA_QUIZ_ID = "extra_quiz_id"

        fun createIntent(context: Context, quizId: String): Intent {
            return Intent(context, QuizTakingActivity::class.java).apply {
                putExtra(EXTRA_QUIZ_ID, quizId)
            }
        }
    }

    private val viewModel: QuizTakingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val quizId = intent.getStringExtra(EXTRA_QUIZ_ID) ?: "sample-quiz-1"

        setContent {
            ElearningplatformTheme {
                val uiState by viewModel.uiState.collectAsState()

                LaunchedEffect(quizId) {
                    viewModel.loadQuiz(quizId)
                }

                QuizTakingScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}
