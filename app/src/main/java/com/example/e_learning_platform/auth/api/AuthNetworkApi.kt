package com.example.e_learning_platform.auth.api

import com.example.e_learning_platform.auth.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class LoginRequestDto(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class RegisterRequestDto(
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

@Serializable
data class AuthResponseDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("token") val token: String? = null,
    @SerialName("user") val user: User? = null
) {
    fun toUser(): User? {
        if (user != null) return user
        if (!id.isNullOrBlank() && !name.isNullOrBlank() && !email.isNullOrBlank()) {
            return User(id = id, name = name, email = email)
        }
        return null
    }
}

interface AuthNetworkApi {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>
}
