package com.kamath.taleweaver.home.account.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.navigation.NavigationEvent
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.core.util.UiEvent
import com.kamath.taleweaver.home.account.domain.usecase.GetUserProfileUseCase
import com.kamath.taleweaver.home.account.domain.usecase.LogoutUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class AccountScreenState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = true
)

sealed interface AccountScreenEvent {
    object OnLogoutClick : AccountScreenEvent
}

@HiltViewModel
class AccountScreenViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountScreenState())
    val uiState = _uiState.asStateFlow()
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadUserProfile()
    }

    fun onEvent(event: AccountScreenEvent) {
        when (event) {
            is AccountScreenEvent.OnLogoutClick -> {
                logout()
            }
        }
    }

    private fun loadUserProfile() {
        getUserProfileUseCase().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true
                    )
                }

                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userProfile = result.data
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                    _eventFlow.emit(
                        UiEvent.ShowSnackbar(
                            result.message ?: "An unknown error occurred"
                        )
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun logout() {
        logoutUseCase().onEach { result ->
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
                    _navigationEvent.emit(NavigationEvent.NavigateToLogin)
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false
                    )
                    _eventFlow.emit(
                        UiEvent.ShowSnackbar(
                            result.message ?: "Logout failed"
                        )
                    )
                    _navigationEvent.emit(NavigationEvent.NavigateToHome)
                }
            }
        }.launchIn(viewModelScope)
    }
}
