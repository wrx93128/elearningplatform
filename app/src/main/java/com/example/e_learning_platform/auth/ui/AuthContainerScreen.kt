package com.example.e_learning_platform.auth.ui

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.e_learning_platform.auth.viewmodel.AuthMode
import com.example.e_learning_platform.auth.viewmodel.AuthUiState
import com.example.e_learning_platform.auth.viewmodel.AuthViewModel

@Composable
fun AuthContainerScreen(
    authViewModel: AuthViewModel,
    uiState: AuthUiState,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = uiState.authMode,
        label = "AuthModeCrossfade",
        modifier = modifier
    ) { mode ->
        when (mode) {
            AuthMode.LOGIN -> {
                LoginScreen(
                    uiState = uiState,
                    onEmailChanged = authViewModel::onLoginEmailChanged,
                    onPasswordChanged = authViewModel::onLoginPasswordChanged,
                    onTogglePasswordVisibility = authViewModel::toggleLoginPasswordVisibility,
                    onLoginSubmitted = authViewModel::submitLogin,
                    onNavigateToRegister = authViewModel::switchToRegister
                )
            }
            AuthMode.REGISTER -> {
                RegisterScreen(
                    uiState = uiState,
                    onNameChanged = authViewModel::onRegisterNameChanged,
                    onEmailChanged = authViewModel::onRegisterEmailChanged,
                    onPasswordChanged = authViewModel::onRegisterPasswordChanged,
                    onConfirmPasswordChanged = authViewModel::onRegisterConfirmPasswordChanged,
                    onTogglePasswordVisibility = authViewModel::toggleRegisterPasswordVisibility,
                    onToggleConfirmPasswordVisibility = authViewModel::toggleRegisterConfirmPasswordVisibility,
                    onRegisterSubmitted = authViewModel::submitRegister,
                    onNavigateToLogin = authViewModel::switchToLogin
                )
            }
        }
    }
}
