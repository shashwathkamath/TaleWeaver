package com.kamath.taleweaver.registration.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamath.taleweaver.core.navigation.NavigationEvent
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.registration.domain.model.RegistrationData
import com.kamath.taleweaver.registration.domain.usecases.RegisterUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistrationScreenState(
    val username: String,
    val email: String,
    val password: String,
    val isLoading: Boolean = false,
    val isButtonEnabled: Boolean = false,
)

sealed interface RegistrationScreenEvent {
    data class OnUsernameChange(val username: String) : RegistrationScreenEvent
    data class OnPasswordChange(val password: String) : RegistrationScreenEvent
    data class OnEmailChange(val email: String) : RegistrationScreenEvent
    object OnSignUpButtonPress : RegistrationScreenEvent
}

sealed interface ResultEvent {
    data class ShowSnackbar(val message: String) : ResultEvent
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

    private val _eventFlow = MutableSharedFlow<ResultEvent>()
    val eventFlow = _eventFlow.asSharedFlow()
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

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
                isButtonEnabled = false
            )
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(
                isButtonEnabled = false
            )
            viewModelScope.launch {
                _eventFlow.emit(
                    ResultEvent.ShowSnackbar("Password must be at least 6 characters long")
                )
            }
            return
        }
        val registrationData = RegistrationData(username, email, password)
        registerUserUseCase(registrationData).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true
                    )
                }

                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                    )
                    _eventFlow.emit(ResultEvent.ShowSnackbar("Sign up successful"))
                    _navigationEvent.emit(NavigationEvent.NavigateToLogin)
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                    )
                    _eventFlow.emit(
                        ResultEvent.ShowSnackbar(
                            result.message.toString() ?: "Sign up failed"
                        )
                    )
                }
            }
        }.launchIn(viewModelScope)
    }
}