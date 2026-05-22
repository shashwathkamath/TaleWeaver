package com.taleweaver.app.login.presentation

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

data class LoginUiState(
    val email: String = "",
    val isButtonEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LoginUiEvent {
    data class OnEmailChange(val email: String) : LoginUiEvent
    object SendCodeButtonPress : LoginUiEvent
    object ErrorDismissed : LoginUiEvent
}

@HiltViewModel
class LoginScreenViewmodel @Inject constructor(
    private val sendOtpUseCase: SendOtpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.OnEmailChange -> _uiState.value = _uiState.value.copy(
                email = event.email,
                isButtonEnabled = event.email.isNotBlank()
            )
            LoginUiEvent.SendCodeButtonPress -> sendCode()
            LoginUiEvent.ErrorDismissed -> _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }

    private fun sendCode() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your email address.")
            return
        }
        sendOtpUseCase(email).onEach { result ->
            when (result) {
                is ApiResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _navigationEvent.emit(NavigationEvent.NavigateToOtp(email))
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }.launchIn(viewModelScope)
    }
}
