package com.example.e_learning_platform.auth.model

import android.util.Patterns

object AuthValidation {

    fun validateName(name: String): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Imię i nazwisko nie może być puste."
            trimmed.length < 2 -> "Imię i nazwisko musi mieć co najmniej 2 znaki."
            else -> null
        }
    }

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> "Adres e-mail nie może być pusty."
            !isValidEmailFormat(trimmed) -> "Nieprawidłowy format adresu e-mail."
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Hasło nie może być puste."
            password.length < 6 -> "Hasło musi mieć co najmniej 6 znaków."
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isEmpty() -> "Potwierdzenie hasła nie może być puste."
            password != confirmPassword -> "Hasła muszą być identyczne."
            else -> null
        }
    }

    private fun isValidEmailFormat(email: String): Boolean {
        // Fallback for JVM unit tests where Patterns.EMAIL_ADDRESS might not be mocked
        val simpleEmailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return simpleEmailRegex.matches(email)
    }
}
