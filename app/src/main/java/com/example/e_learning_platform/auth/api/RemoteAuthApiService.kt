package com.example.e_learning_platform.auth.api

import com.example.e_learning_platform.auth.model.AuthValidation
import com.example.e_learning_platform.auth.model.User
import com.example.e_learning_platform.network.NetworkClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.Response

class RemoteAuthApiService(
    private val api: AuthNetworkApi = NetworkClient.createService()
) : AuthApiService {

    override suspend fun login(email: String, password: String): Result<User> {
        val emailError = AuthValidation.validateEmail(email)
        if (emailError != null) {
            return Result.failure(IllegalArgumentException("401 Unauthorized"))
        }

        return runCatching {
            val response = api.login(LoginRequestDto(email = email.trim(), password = password))
            if (response.isSuccessful) {
                val body = response.body()
                val user = body?.toUser()
                if (user != null) {
                    user
                } else {
                    throw IllegalArgumentException("Błędna odpowiedź serwera (brak danych użytkownika)")
                }
            } else {
                val errorMessage = parseErrorMessage(response)
                throw IllegalArgumentException(errorMessage)
            }
        }.recoverCatching { throwable ->
            if (throwable is IllegalArgumentException) {
                throw throwable
            }
            throw java.io.IOException("Nie można połączyć się z serwerem API. Upewnij się, że backend działa.", throwable)
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        val nameError = AuthValidation.validateName(name)
        if (nameError != null) return Result.failure(IllegalArgumentException(nameError))

        val emailError = AuthValidation.validateEmail(email)
        if (emailError != null) return Result.failure(IllegalArgumentException(emailError))

        val passwordError = AuthValidation.validatePassword(password)
        if (passwordError != null) return Result.failure(IllegalArgumentException(passwordError))

        return runCatching {
            val response = api.register(
                RegisterRequestDto(
                    name = name.trim(),
                    email = email.trim().lowercase(),
                    password = password
                )
            )
            if (response.isSuccessful) {
                val body = response.body()
                val user = body?.toUser()
                if (user != null) {
                    user
                } else {
                    throw IllegalArgumentException("Błędna odpowiedź serwera (brak danych użytkownika)")
                }
            } else {
                val errorMessage = parseErrorMessage(response)
                throw IllegalArgumentException(errorMessage)
            }
        }.recoverCatching { throwable ->
            if (throwable is IllegalArgumentException) {
                throw throwable
            }
            throw java.io.IOException("Nie można połączyć się z serwerem API. Upewnij się, że backend działa.", throwable)
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

        return when (code) {
            401 -> "401 Unauthorized"
            409 -> "Konto o tym adresie e-mail już istnieje."
            else -> "Błąd serwera ($code)"
        }
    }
}
