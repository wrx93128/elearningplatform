package com.example.e_learning_platform.auth

import com.example.e_learning_platform.auth.api.MockAuthApiService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockAuthApiServiceTest {

    private val apiService = MockAuthApiService(networkDelayMs = 0)

    @Test
    fun `login succeeds with admin credentials`() = runTest {
        val result = apiService.login("admin@admin.com", "admin")
        assertTrue(result.isSuccess)
        val user = result.getOrThrow()
        assertEquals("Admin", user.name)
        assertEquals("admin@admin.com", user.email)
    }

    @Test
    fun `login fails with wrong password`() = runTest {
        val result = apiService.login("admin@admin.com", "wrongpass")
        assertTrue(result.isFailure)
        assertEquals("401 Unauthorized", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login fails with non-existing email`() = runTest {
        val result = apiService.login("unknown@example.com", "password123")
        assertTrue(result.isFailure)
        assertEquals("401 Unauthorized", result.exceptionOrNull()?.message)
    }

    @Test
    fun `register succeeds for new user and enables login`() = runTest {
        val regResult = apiService.register("Anna Nowak", "anna.nowak@example.com", "secret123")
        assertTrue(regResult.isSuccess)
        val registeredUser = regResult.getOrThrow()
        assertEquals("Anna Nowak", registeredUser.name)
        assertEquals("anna.nowak@example.com", registeredUser.email)

        // Verify newly registered user can log in
        val loginResult = apiService.login("anna.nowak@example.com", "secret123")
        assertTrue(loginResult.isSuccess)
    }

    @Test
    fun `register fails if email already exists`() = runTest {
        val regResult = apiService.register("Admin Duplicate", "admin@admin.com", "admin123")
        assertTrue(regResult.isFailure)
        assertEquals("Konto o tym adresie e-mail już istnieje.", regResult.exceptionOrNull()?.message)
    }
}
