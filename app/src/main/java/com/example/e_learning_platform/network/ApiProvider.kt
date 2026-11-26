package com.example.e_learning_platform.network

import com.example.e_learning_platform.auth.api.AuthApiService
import com.example.e_learning_platform.auth.api.MockAuthApiService
import com.example.e_learning_platform.auth.api.RemoteAuthApiService
import com.example.e_learning_platform.quiz.api.MockQuizApiService
import com.example.e_learning_platform.quiz.api.QuizApiService
import com.example.e_learning_platform.quiz.api.RemoteQuizApiService

object ApiProvider {

    private val mockAuthApiService by lazy { MockAuthApiService() }
    private val remoteAuthApiService by lazy { RemoteAuthApiService() }

    private val mockQuizApiService by lazy { MockQuizApiService() }
    private val remoteQuizApiService by lazy { RemoteQuizApiService() }

    val authApiService: AuthApiService
        get() = if (ApiConfig.useMockApi) mockAuthApiService else remoteAuthApiService

    val quizApiService: QuizApiService
        get() = if (ApiConfig.useMockApi) mockQuizApiService else remoteQuizApiService
}
