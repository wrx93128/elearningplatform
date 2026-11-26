package com.example.e_learning_platform.quiz.api

import com.example.e_learning_platform.network.NetworkClient
import com.example.e_learning_platform.quiz.model.CreateQuizRequest
import com.example.e_learning_platform.quiz.model.Quiz
import com.example.e_learning_platform.quiz.model.QuizResult
import com.example.e_learning_platform.quiz.model.QuizSubmissionRequest
import com.example.e_learning_platform.quiz.model.QuizSummary
import com.example.e_learning_platform.quiz.model.QuizValidationResult
import com.example.e_learning_platform.quiz.model.QuizValidator
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.Response

class RemoteQuizApiService(
    private val api: QuizNetworkApi = NetworkClient.createService()
) : QuizApiService {

    override suspend fun createQuiz(request: CreateQuizRequest): Result<Quiz> {
        when (val validation = QuizValidator.validateCreateQuiz(request)) {
            is QuizValidationResult.Invalid -> {
                return Result.failure(IllegalArgumentException(validation.errorMessage))
            }
            is QuizValidationResult.Valid -> {}
        }

        return runCatching {
            val response = api.createQuiz(request)
            if (response.isSuccessful) {
                response.body() ?: throw IllegalArgumentException("Serwer zwrócił pustą odpowiedź.")
            } else {
                val errorMessage = parseErrorMessage(response)
                throw IllegalArgumentException(errorMessage)
            }
        }.recoverCatching { throwable ->
            if (throwable is IllegalArgumentException) throw throwable
            throw java.io.IOException("Błąd połączenia z serwerem podczas tworzenia quizu.", throwable)
        }
    }

    override suspend fun getQuizzes(): Result<List<QuizSummary>> {
        return runCatching {
            val response = api.getQuizzes()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                val errorMessage = parseErrorMessage(response)
                throw IllegalArgumentException(errorMessage)
            }
        }.recoverCatching { throwable ->
            if (throwable is IllegalArgumentException) throw throwable
            throw java.io.IOException("Błąd pobierania listy quizów z serwera.", throwable)
        }
    }

    override suspend fun getQuizById(id: String): Result<Quiz> {
        return runCatching {
            val response = api.getQuizById(id)
            if (response.isSuccessful) {
                response.body() ?: throw NoSuchElementException("Quiz z ID '$id' nie został znaleziony.")
            } else if (response.code() == 404) {
                throw NoSuchElementException("Quiz z ID '$id' nie istnieje na serwerze.")
            } else {
                val errorMessage = parseErrorMessage(response)
                throw IllegalArgumentException(errorMessage)
            }
        }.recoverCatching { throwable ->
            if (throwable is NoSuchElementException || throwable is IllegalArgumentException) throw throwable
            throw java.io.IOException("Błąd pobierania quizu z serwera.", throwable)
        }
    }

    override suspend fun submitQuiz(submission: QuizSubmissionRequest): Result<QuizResult> {
        return runCatching {
            var response = api.submitQuiz(submission)
            if (response.code() == 404) {
                // Fallback do endpointu z ID quizu w ścieżce /api/quizzes/{id}/submit
                response = api.submitQuizWithId(submission.quizId, submission)
            }

            if (response.isSuccessful) {
                response.body() ?: throw IllegalArgumentException("Serwer zwrócił pusty wynik quizu.")
            } else if (response.code() == 404) {
                throw NoSuchElementException("Quiz z ID '${submission.quizId}' nie został znaleziony.")
            } else {
                val errorMessage = parseErrorMessage(response)
                throw IllegalArgumentException(errorMessage)
            }
        }.recoverCatching { throwable ->
            if (throwable is NoSuchElementException || throwable is IllegalArgumentException) throw throwable
            throw java.io.IOException("Błąd wysyłania odpowiedzi na serwer.", throwable)
        }
    }

    private fun <T> parseErrorMessage(response: Response<T>): String {
        val code = response.code()
        val rawError = response.errorBody()?.string()

        if (!rawError.isNullOrBlank()) {
            runCatching {
                val json = NetworkClient.json.parseToJsonElement(rawError).jsonObject
                val detail = json["detail"]?.jsonPrimitive?.content
                if (!detail.isNullOrBlank()) return detail
                val message = json["message"]?.jsonPrimitive?.content
                if (!message.isNullOrBlank()) return message
            }
        }

        return "Błąd serwera ($code)"
    }
}
