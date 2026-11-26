package com.example.e_learning_platform.quiz.api

import com.example.e_learning_platform.quiz.model.CreateQuizRequest
import com.example.e_learning_platform.quiz.model.Quiz
import com.example.e_learning_platform.quiz.model.QuizResult
import com.example.e_learning_platform.quiz.model.QuizSubmissionRequest
import com.example.e_learning_platform.quiz.model.QuizSummary
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface QuizNetworkApi {

    @POST("api/quizzes")
    suspend fun createQuiz(@Body request: CreateQuizRequest): Response<Quiz>

    @GET("api/quizzes")
    suspend fun getQuizzes(): Response<List<QuizSummary>>

    @GET("api/quizzes/{id}")
    suspend fun getQuizById(@Path("id") id: String): Response<Quiz>

    @POST("api/quizzes/submit")
    suspend fun submitQuiz(@Body submission: QuizSubmissionRequest): Response<QuizResult>

    @POST("api/quizzes/{id}/submit")
    suspend fun submitQuizWithId(
        @Path("id") id: String,
        @Body submission: QuizSubmissionRequest
    ): Response<QuizResult>
}
