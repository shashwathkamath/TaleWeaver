package com.kamath.taleweaver.signUp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.signUp.domain.model.User
import com.kamath.taleweaver.signUp.domain.usecases.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class RegistrationScreenState(
    val username: String,
    val email: String,
    val password: String,
    val isLoading: Boolean = false,
    val isButtonEnabled: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

sealed interface RegistrationScreenEvent {
    data class OnUsernameChange(val username: String) : RegistrationScreenEvent
    data class OnPasswordChange(val password: String) : RegistrationScreenEvent
    data class OnEmailChange(val email: String) : RegistrationScreenEvent
    object OnSignUpButtonPress : RegistrationScreenEvent
}

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        RegistrationScreenState(
            username = "",
            email = "",
            password = ""
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: RegistrationScreenEvent) {
        when (event) {
            is RegistrationScreenEvent.OnUsernameChange -> {
                _uiState.value = _uiState.value.copy(
                    username = event.username,
                    isButtonEnabled = event.username.isNotBlank()
                            && _uiState.value.email.isNotBlank()
                            && _uiState.value.password.isNotBlank()
                )
            }

            is RegistrationScreenEvent.OnEmailChange -> {
                _uiState.value = _uiState.value.copy(
                    email = event.email,
                    isButtonEnabled = event.email.isNotBlank()
                            && _uiState.value.username.isNotBlank()
                            && _uiState.value.password.isNotBlank()
                )
            }

            is RegistrationScreenEvent.OnPasswordChange -> {
                _uiState.value = _uiState.value.copy(
                    password = event.password,
                    isButtonEnabled = event.password.isNotBlank()
                            && _uiState.value.email.isNotBlank()
                            && _uiState.value.username.isNotBlank()
                )
            }

            is RegistrationScreenEvent.OnSignUpButtonPress -> {
                signUp()
            }
        }
    }

    private fun signUp() {
        val username = _uiState.value.username
        val password = _uiState.value.password
        val email = _uiState.value.email
        if (username.isBlank() || password.isBlank() || email.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "All fields must be filled",
                isButtonEnabled = false
            )
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(
                errorMessage =
                    "Password must be at least 6 characters",
                isButtonEnabled = false
            )

            return
        }
        val user = User(username, email, password)
        registerUserUseCase(user).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true
                    )
                }

                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.message ?: "Sign Up Successful"
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Could not sign you"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }
}