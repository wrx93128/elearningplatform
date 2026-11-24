package com.example.e_learning_platform.auth

import com.example.e_learning_platform.auth.model.AuthValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidationTest {

    @Test
    fun `validateName returns error for empty or single char name`() {
        assertEquals("Imię i nazwisko nie może być puste.", AuthValidation.validateName(""))
        assertEquals("Imię i nazwisko nie może być puste.", AuthValidation.validateName("   "))
        assertEquals("Imię i nazwisko musi mieć co najmniej 2 znaki.", AuthValidation.validateName("A"))
    }

    @Test
    fun `validateName returns null for valid name`() {
        assertNull(AuthValidation.validateName("Jan Kowalski"))
    }

    @Test
    fun `validateEmail returns error for invalid email`() {
        assertEquals("Adres e-mail nie może być pusty.", AuthValidation.validateEmail(""))
        assertEquals("Nieprawidłowy format adresu e-mail.", AuthValidation.validateEmail("invalid-email"))
        assertEquals("Nieprawidłowy format adresu e-mail.", AuthValidation.validateEmail("test@"))
    }

    @Test
    fun `validateEmail returns null for valid email`() {
        assertNull(AuthValidation.validateEmail("test@example.com"))
        assertNull(AuthValidation.validateEmail("jan.kowalski@domain.org"))
    }

    @Test
    fun `validatePassword returns error for short or empty password`() {
        assertEquals("Hasło nie może być puste.", AuthValidation.validatePassword(""))
        assertEquals("Hasło musi mieć co najmniej 6 znaków.", AuthValidation.validatePassword("12345"))
    }

    @Test
    fun `validatePassword returns null for valid password`() {
        assertNull(AuthValidation.validatePassword("123456"))
        assertNull(AuthValidation.validatePassword("secretPassword"))
    }

    @Test
    fun `validateConfirmPassword checks password equality`() {
        assertEquals("Potwierdzenie hasła nie może być puste.", AuthValidation.validateConfirmPassword("pass123", ""))
        assertEquals("Hasła muszą być identyczne.", AuthValidation.validateConfirmPassword("pass123", "pass456"))
        assertNull(AuthValidation.validateConfirmPassword("pass123", "pass123"))
    }
}
