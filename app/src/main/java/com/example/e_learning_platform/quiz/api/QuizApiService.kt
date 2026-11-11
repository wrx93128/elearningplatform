package com.example.e_learning_platform.quiz.api

import com.example.e_learning_platform.quiz.model.CreateQuizRequest
import com.example.e_learning_platform.quiz.model.Quiz
import com.example.e_learning_platform.quiz.model.QuizResult
import com.example.e_learning_platform.quiz.model.QuizSubmissionRequest
import com.example.e_learning_platform.quiz.model.QuizSummary

interface QuizApiService {

    suspend fun createQuiz(request: CreateQuizRequest): Result<Quiz>

    suspend fun getQuizzes(): Result<List<QuizSummary>>

    suspend fun getQuizById(id: String): Result<Quiz>

    suspend fun submitQuiz(submission: QuizSubmissionRequest): Result<QuizResult>
}
