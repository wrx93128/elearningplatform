package com.example.e_learning_platform.auth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_learning_platform.auth.api.AuthApiService
import com.example.e_learning_platform.auth.api.MockAuthApiService
import com.example.e_learning_platform.auth.data.ISessionManager
import com.example.e_learning_platform.auth.data.SessionManager
import com.example.e_learning_platform.auth.model.AuthValidation
import com.example.e_learning_platform.auth.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN,
    REGISTER
}

sealed interface AuthSessionState {
    data object Checking : AuthSessionState
    data object Unauthenticated : AuthSessionState
    data class Authenticated(val user: User) : AuthSessionState
}

data class AuthUiState(
    val authMode: AuthMode = AuthMode.LOGIN,
    val sessionState: AuthSessionState = AuthSessionState.Checking,

    // Login form
    val loginEmail: String = "",
    val loginPassword: String = "",
    val loginEmailError: String? = null,
    val loginPasswordError: String? = null,
    val loginIsPasswordVisible: Boolean = false,
    val loginGeneralError: String? = null,
    val isLoginLoading: Boolean = false,

    // Register form
    val registerName: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",
    val registerConfirmPassword: String = "",
    val registerNameError: String? = null,
    val registerEmailError: String? = null,
    val registerPasswordError: String? = null,
    val registerConfirmPasswordError: String? = null,
    val registerIsPasswordVisible: Boolean = false,
    val registerIsConfirmPasswordVisible: Boolean = false,
    val registerGeneralError: String? = null,
    val isRegisterLoading: Boolean = false
)

class AuthViewModel @JvmOverloads constructor(
    application: Application,
    private val authApiService: AuthApiService = MockAuthApiService.default,
    private val sessionManager: ISessionManager = SessionManager(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        val user = sessionManager.getCurrentUser()

        val initialSession = if (user != null) {
            AuthSessionState.Authenticated(user)
        } else {
            AuthSessionState.Unauthenticated
        }

        _uiState.update { it.copy(sessionState = initialSession) }
    }

    fun switchToLogin() {
        _uiState.update {
            it.copy(
                authMode = AuthMode.LOGIN,
                loginGeneralError = null,
                registerGeneralError = null
            )
        }
    }

    fun switchToRegister() {
        _uiState.update {
            it.copy(
                authMode = AuthMode.REGISTER,
                loginGeneralError = null,
                registerGeneralError = null
            )
        }
    }

    // Login Form Actions
    fun onLoginEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                loginEmail = email,
                loginEmailError = null,
                loginGeneralError = null
            )
        }
    }

    fun onLoginPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                loginPassword = password,
                loginPasswordError = null,
                loginGeneralError = null
            )
        }
    }

    fun toggleLoginPasswordVisibility() {
        _uiState.update {
            it.copy(loginIsPasswordVisible = !it.loginIsPasswordVisible)
        }
    }

    fun submitLogin() {
        val currentState = _uiState.value
        val emailErr = AuthValidation.validateEmail(currentState.loginEmail)

        if (emailErr != null) {
            _uiState.update {
                it.copy(
                    loginEmailError = emailErr,
                    loginPasswordError = null
                )
            }
            return
        }

        _uiState.update { it.copy(isLoginLoading = true, loginGeneralError = null) }

        viewModelScope.launch {
            val result = authApiService.login(currentState.loginEmail, currentState.loginPassword)
            result.fold(
                onSuccess = { user ->
                    sessionManager.saveUserSession(user)
                    _uiState.update {
                        it.copy(
                            isLoginLoading = false,
                            sessionState = AuthSessionState.Authenticated(user)
                        )
                    }
                },
                onFailure = { _ ->
                    // Frontend hardcoded generic error message on failed login (401)
                    _uiState.update {
                        it.copy(
                            isLoginLoading = false,
                            loginGeneralError = "Nieprawidłowy adres e-mail lub hasło."
                        )
                    }
                }
            )
        }
    }

    // Register Form Actions
    fun onRegisterNameChanged(name: String) {
        _uiState.update {
            it.copy(
                registerName = name,
                registerNameError = null,
                registerGeneralError = null
            )
        }
    }

    fun onRegisterEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                registerEmail = email,
                registerEmailError = null,
                registerGeneralError = null
            )
        }
    }

    fun onRegisterPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                registerPassword = password,
                registerPasswordError = null,
                registerGeneralError = null
            )
        }
    }

    fun onRegisterConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update {
            it.copy(
                registerConfirmPassword = confirmPassword,
                registerConfirmPasswordError = null,
                registerGeneralError = null
            )
        }
    }

    fun toggleRegisterPasswordVisibility() {
        _uiState.update {
            it.copy(registerIsPasswordVisible = !it.registerIsPasswordVisible)
        }
    }

    fun toggleRegisterConfirmPasswordVisibility() {
        _uiState.update {
            it.copy(registerIsConfirmPasswordVisible = !it.registerIsConfirmPasswordVisible)
        }
    }

    fun submitRegister() {
        val currentState = _uiState.value
        val nameErr = AuthValidation.validateName(currentState.registerName)
        val emailErr = AuthValidation.validateEmail(currentState.registerEmail)
        val passwordErr = AuthValidation.validatePassword(currentState.registerPassword)
        val confirmPasswordErr = AuthValidation.validateConfirmPassword(
            currentState.registerPassword,
            currentState.registerConfirmPassword
        )

        if (nameErr != null || emailErr != null || passwordErr != null || confirmPasswordErr != null) {
            _uiState.update {
                it.copy(
                    registerNameError = nameErr,
                    registerEmailError = emailErr,
                    registerPasswordError = passwordErr,
                    registerConfirmPasswordError = confirmPasswordErr
                )
            }
            return
        }

        _uiState.update { it.copy(isRegisterLoading = true, registerGeneralError = null) }

        viewModelScope.launch {
            val result = authApiService.register(
                currentState.registerName,
                currentState.registerEmail,
                currentState.registerPassword
            )
            result.fold(
                onSuccess = { user ->
                    sessionManager.saveUserSession(user)
                    _uiState.update {
                        it.copy(
                            isRegisterLoading = false,
                            sessionState = AuthSessionState.Authenticated(user)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isRegisterLoading = false,
                            registerGeneralError = error.message ?: "Błąd rejestracji."
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _uiState.update {
            it.copy(
                sessionState = AuthSessionState.Unauthenticated,
                authMode = AuthMode.LOGIN,
                loginEmail = "",
                loginPassword = "",
                loginEmailError = null,
                loginPasswordError = null,
                loginGeneralError = null,
                registerName = "",
                registerEmail = "",
                registerPassword = "",
                registerConfirmPassword = "",
                registerNameError = null,
                registerEmailError = null,
                registerPasswordError = null,
                registerConfirmPasswordError = null,
                registerGeneralError = null
            )
        }
    }
}
