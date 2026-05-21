package com.taleweaver.app.registration.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taleweaver.app.core.navigation.NavigationEvent
import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.login.domain.usecases.SendOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class RegistrationScreenState(
    val username: String = "",
    val email: String = "",
    val isLoading: Boolean = false,
    val isButtonEnabled: Boolean = false,
    val errorMessage: String? = null
)

sealed interface RegistrationScreenEvent {
    data class OnUsernameChange(val username: String) : RegistrationScreenEvent
    data class OnEmailChange(val email: String) : RegistrationScreenEvent
    object OnSignUpButtonPress : RegistrationScreenEvent
    object ErrorDismissed : RegistrationScreenEvent
}

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val sendOtpUseCase: SendOtpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationScreenState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEvent(event: RegistrationScreenEvent) {
        when (event) {
            is RegistrationScreenEvent.OnUsernameChange -> _uiState.value = _uiState.value.copy(
                username = event.username,
                isButtonEnabled = _uiState.value.email.isNotBlank()
            )
            is RegistrationScreenEvent.OnEmailChange -> _uiState.value = _uiState.value.copy(
                email = event.email,
                isButtonEnabled = event.email.isNotBlank()
            )
            RegistrationScreenEvent.OnSignUpButtonPress -> signUp()
            RegistrationScreenEvent.ErrorDismissed -> _uiState.value =
                _uiState.value.copy(errorMessage = null)
        }
    }

    private fun signUp() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) return
        val username = _uiState.value.username.trim().ifBlank {
            "reader_${(1000..9999).random()}"
        }

        sendOtpUseCase(email).onEach { result ->
            when (result) {
                is ApiResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _navigationEvent.emit(NavigationEvent.NavigateToOtp(email, username))
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }.launchIn(viewModelScope)
    }
}
