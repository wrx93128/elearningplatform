package com.example.e_learning_platform.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String
)

