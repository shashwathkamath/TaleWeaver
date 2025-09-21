package com.kamath.taleweaver.signup.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SignUpScreenState {
    object Loading : SignUpScreenState()
    data class Input(
        val email: String,
        val username: String,
        val password: String,
        val isButtonEnabled: Boolean
    ) : SignUpScreenState()

    data class Success(val message: String) : SignUpScreenState()
    data class Error(val message: String) : SignUpScreenState()

}

sealed class SignUpScreenEvent() {
    data class OnEmailChange(val email: String) : SignUpScreenEvent()
    data class OnUsernameChange(val username: String) : SignUpScreenEvent()
    data class OnPasswordChange(val password: String) : SignUpScreenEvent()
    object OnSignUpClick : SignUpScreenEvent()
}

sealed class SignUpScreenAction() {
    data class ShowToast(val message: String) : SignUpScreenAction()
    object NavigateToLogin : SignUpScreenAction()
}

@HiltViewModel
class SignUpViewmodel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpScreenState>(
        SignUpScreenState.Input(
            email = "",
            username = "",
            password = "",
            isButtonEnabled = false
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _uiAction = MutableSharedFlow<SignUpScreenAction>(replay = 0)
    val uiAction = _uiAction.asSharedFlow()

    fun onEvent(event: SignUpScreenEvent) {
        when (event) {
            is SignUpScreenEvent.OnEmailChange -> {
                val currentState = _uiState.value
                if (currentState is SignUpScreenState.Input) {
                    val email = event.email
                    val isButtonEnabled = email.isNotEmpty() &&
                            currentState.password.isNotEmpty() && currentState.username.isNotEmpty()
                    _uiState.value = currentState.copy(
                        email = email,
                        isButtonEnabled = isButtonEnabled
                    )
                }
            }

            is SignUpScreenEvent.OnUsernameChange -> {
                val currentState = _uiState.value
                if (currentState is SignUpScreenState.Input) {
                    val username = event.username
                    val isButtonEnabled =
                        username.isNotEmpty() && currentState.password.isNotEmpty()
                                && currentState.email.isNotEmpty()
                    _uiState.value = currentState.copy(
                        username = username,
                        isButtonEnabled = isButtonEnabled
                    )
                }
            }

            is SignUpScreenEvent.OnPasswordChange -> {
                val currentState = _uiState.value
                if (currentState is SignUpScreenState.Input) {
                    val password = event.password
                    val isButtonEnabled =
                        password.isNotEmpty() && currentState.email.isNotEmpty() && currentState.username.isNotEmpty()
                    _uiState.value = currentState.copy(
                        password = password,
                        isButtonEnabled = isButtonEnabled
                    )
                }
            }

            is SignUpScreenEvent.OnSignUpClick -> {
                viewModelScope.launch {
                    _uiAction.emit(SignUpScreenAction.NavigateToLogin)
                }
            }
        }
    }
}