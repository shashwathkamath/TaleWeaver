package com.kamath.taleweaver.login.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamath.taleweaver.core.navigation.AppDestination
import com.kamath.taleweaver.core.navigation.NavigationEvent
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.login.domain.usecases.CreateUserProfileUseCase
import com.kamath.taleweaver.login.domain.usecases.SendOtpUseCase
import com.kamath.taleweaver.login.domain.usecases.VerifyOtpAndSignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OtpUiState(
    val email: String = "",
    val otpValue: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val remainingSeconds: Int = 60,
    val canResend: Boolean = false
)

sealed interface OtpUiEvent {
    data class OnOtpChanged(val value: String) : OtpUiEvent
    object VerifyButtonPress : OtpUiEvent
    object ResendCode : OtpUiEvent
    object ErrorDismissed : OtpUiEvent
}

@HiltViewModel
class OtpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val verifyOtpAndSignInUseCase: VerifyOtpAndSignInUseCase,
    private val createUserProfileUseCase: CreateUserProfileUseCase,
    private val sendOtpUseCase: SendOtpUseCase
) : ViewModel() {

    private val email = savedStateHandle.get<String>(AppDestination.ARG_OTP_EMAIL) ?: ""
    private val username = savedStateHandle.get<String>(AppDestination.ARG_OTP_USERNAME)
        ?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(OtpUiState(email = email))
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private var countdownJob: Job? = null

    init {
        startResendCountdown()
    }

    fun onEvent(event: OtpUiEvent) {
        when (event) {
            is OtpUiEvent.OnOtpChanged -> {
                _uiState.value = _uiState.value.copy(otpValue = event.value)
                if (event.value.length == 6) verify(event.value)
            }
            OtpUiEvent.VerifyButtonPress -> verify(_uiState.value.otpValue)
            OtpUiEvent.ResendCode -> resend()
            OtpUiEvent.ErrorDismissed -> _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }

    private fun verify(code: String) {
        if (code.length != 6 || _uiState.value.isLoading) return
        verifyOtpAndSignInUseCase(email, code).onEach { result ->
            when (result) {
                is ApiResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                is ApiResult.Success -> {
                    val uid = result.data!!
                    if (username != null) {
                        createProfileAndNavigate(uid)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _navigationEvent.emit(NavigationEvent.NavigateToHome)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        otpValue = ""
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun createProfileAndNavigate(uid: String) {
        createUserProfileUseCase(uid, email, username!!).onEach { result ->
            when (result) {
                is ApiResult.Loading -> {}
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _navigationEvent.emit(NavigationEvent.NavigateToHome)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun resend() {
        sendOtpUseCase(email).onEach { result ->
            when (result) {
                is ApiResult.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, otpValue = "")
                    startResendCountdown()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun startResendCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(canResend = false, remainingSeconds = 60)
            for (i in 59 downTo 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    remainingSeconds = i,
                    canResend = i == 0
                )
            }
        }
    }
}
