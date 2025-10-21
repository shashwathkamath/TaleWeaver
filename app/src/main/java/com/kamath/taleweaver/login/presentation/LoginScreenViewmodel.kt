package com.kamath.taleweaver.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamath.taleweaver.core.navigation.NavigationEvent
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.login.domain.usecases.LoginUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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

@HiltViewModel
class LoginScreenViewmodel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase
) : ViewModel() {

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
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

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
                login()
            }

            is LoginUiEvent.ErrorDismissed -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }
    }

    private fun login() {
        val email = _uiState.value.email
        val password = _uiState.value.password
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Email and password cannot be empty"
            )
            return
        }
        loginUserUseCase(email, password).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true
                    )
                }

                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Login Successful"
                    )
                    _navigationEvent.emit(NavigationEvent.NavigateToHome)
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "An unknown error occurred"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }
}