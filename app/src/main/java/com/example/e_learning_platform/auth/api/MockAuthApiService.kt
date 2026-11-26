package com.example.e_learning_platform.auth.api

import com.example.e_learning_platform.auth.model.AuthValidation
import com.example.e_learning_platform.auth.model.User
import kotlinx.coroutines.delay
import java.util.UUID

import com.example.e_learning_platform.network.ApiProvider

interface AuthApiService {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>

    companion object {
        val default: AuthApiService get() = ApiProvider.authApiService
    }
}


class MockAuthApiService(
    private val networkDelayMs: Long = 400L
) : AuthApiService {

    private data class MockUserAccount(
        val user: User,
        val passwordHash: String
    )

    private val usersDatabase = mutableMapOf<String, MockUserAccount>()

    init {
        // Pre-populate with admin demo user
        val adminUser = User(
            id = "user-admin-1",
            name = "Admin",
            email = "admin@admin.com"
        )
        usersDatabase[adminUser.email.lowercase()] = MockUserAccount(
            user = adminUser,
            passwordHash = "admin"
        )
    }

    override suspend fun login(email: String, password: String): Result<User> {
        if (networkDelayMs > 0) delay(networkDelayMs)

        val emailError = AuthValidation.validateEmail(email)
        if (emailError != null) return Result.failure(IllegalArgumentException("401 Unauthorized"))

        val account = usersDatabase[email.trim().lowercase()]
            ?: return Result.failure(IllegalArgumentException("401 Unauthorized"))

        if (account.passwordHash != password) {
            return Result.failure(IllegalArgumentException("401 Unauthorized"))
        }

        return Result.success(account.user)
    }

    override suspend fun register(name: String, email: String, password: String): Result<User> {
        if (networkDelayMs > 0) delay(networkDelayMs)

        val nameError = AuthValidation.validateName(name)
        if (nameError != null) return Result.failure(IllegalArgumentException(nameError))

        val emailError = AuthValidation.validateEmail(email)
        if (emailError != null) return Result.failure(IllegalArgumentException(emailError))

        val passwordError = AuthValidation.validatePassword(password)
        if (passwordError != null) return Result.failure(IllegalArgumentException(passwordError))

        val normalizedEmail = email.trim().lowercase()
        if (usersDatabase.containsKey(normalizedEmail)) {
            return Result.failure(IllegalArgumentException("Konto o tym adresie e-mail już istnieje."))
        }

        val newUser = User(
            id = "user-" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            email = normalizedEmail
        )

        usersDatabase[normalizedEmail] = MockUserAccount(
            user = newUser,
            passwordHash = password
        )

        return Result.success(newUser)
    }

    companion object {
        val default: MockAuthApiService by lazy { MockAuthApiService() }
    }
}
