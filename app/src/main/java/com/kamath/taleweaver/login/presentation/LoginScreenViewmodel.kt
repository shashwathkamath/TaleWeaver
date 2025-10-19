package com.kamath.taleweaver.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String,
    val password: String,
    val isButtonEnabled: Boolean,
    val isLoading: Boolean,
    val successMessage: String?,
    val errorMessage: String?
)

sealed interface LoginUiEvent {
    object LoginButtonPress : LoginUiEvent
    data class OnEmailChange(val email: String) : LoginUiEvent
    data class OnPasswordChange(val password: String) : LoginUiEvent
    object ErrorDismissed : LoginUiEvent
}

class LoginScreenViewmodel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(
        LoginUiState(
            email = "",
            password = "",
            isButtonEnabled = false,
            isLoading = false,
            successMessage = null,
            errorMessage = null
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.OnEmailChange -> {
                val email = event.email
                _uiState.value = _uiState.value.copy(
                    email = email,
                    isButtonEnabled = email.isNotBlank() && _uiState.value.password.isNotBlank()
                )
            }

            is LoginUiEvent.OnPasswordChange -> {
                val password = event.password
                _uiState.value = _uiState.value.copy(
                    password = password,
                    isButtonEnabled = password.isNotBlank() && _uiState.value.email.isNotBlank()
                )
            }

            LoginUiEvent.LoginButtonPress -> {
                val email = _uiState.value.email
                val password = _uiState.value.password
                login(email, password)
            }

            is LoginUiEvent.ErrorDismissed -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }
    }

    private fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(2000)
            if (email.isNotBlank() && password.length > 5) {
                _uiState.value = _uiState.value.copy(successMessage = "Login successful!")
                _uiState.value = _uiState.value.copy(isLoading = false)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Login unsuccessful!")
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

}