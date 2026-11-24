package com.example.e_learning_platform.auth

import android.app.Application
import com.example.e_learning_platform.auth.api.MockAuthApiService
import com.example.e_learning_platform.auth.data.ISessionManager
import com.example.e_learning_platform.auth.model.User
import com.example.e_learning_platform.auth.viewmodel.AuthSessionState
import com.example.e_learning_platform.auth.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeSessionManager : ISessionManager {
    private var currentUser: User? = null

    override fun saveUserSession(user: User) {
        currentUser = user
    }

    override fun clearSession() {
        currentUser = null
    }

    override fun isLoggedIn(): Boolean = currentUser != null
    override fun getCurrentUser(): User? = currentUser
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeSessionManager = FakeSessionManager()
    private val apiService = MockAuthApiService(networkDelayMs = 0)

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(
            application = Application(),
            authApiService = apiService,
            sessionManager = fakeSessionManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submitLogin shows frontend generic error on 401 backend response`() = runTest {
        viewModel.onLoginEmailChanged("admin@admin.com")
        viewModel.onLoginPasswordChanged("wrongpassword")

        viewModel.submitLogin()
        testDispatcher.scheduler.advanceUntilIdle()

        // Password error should remain null, frontend displays its generic error
        assertNull(viewModel.uiState.value.loginPasswordError)
        assertEquals("Nieprawidłowy adres e-mail lub hasło.", viewModel.uiState.value.loginGeneralError)
    }

    @Test
    fun `submitLogin succeeds with correct admin credentials`() = runTest {
        viewModel.onLoginEmailChanged("admin@admin.com")
        viewModel.onLoginPasswordChanged("admin")

        viewModel.submitLogin()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.sessionState is AuthSessionState.Authenticated)
        assertEquals("Admin", (viewModel.uiState.value.sessionState as AuthSessionState.Authenticated).user.name)
    }
}
