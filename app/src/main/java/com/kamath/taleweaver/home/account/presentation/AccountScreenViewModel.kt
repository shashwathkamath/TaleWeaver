package com.kamath.taleweaver.home.account.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.navigation.NavigationEvent
import com.kamath.taleweaver.core.util.AddressSuggestion
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.GeocodingService
import com.kamath.taleweaver.core.util.UiEvent
import com.kamath.taleweaver.home.account.domain.usecase.GetUserProfileUseCase
import com.kamath.taleweaver.home.account.domain.usecase.LogoutUserUseCase
import com.kamath.taleweaver.home.account.domain.usecase.UpdateAccountScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed interface AccountScreenState {
    object Loading : AccountScreenState
    data class Success(
        val userProfile: UserProfile?,
        val isSaving: Boolean = false,
        val addressQuery: String = "",
        val addressSuggestions: List<AddressSuggestion> = emptyList(),
        val selectedAddressDisplay: String = "",
        val isLoadingSuggestions: Boolean = false
    ) : AccountScreenState

    data class Error(val message: String) : AccountScreenState
}

sealed interface AccountScreenEvent {
    data class OnDescriptionChange(val description: String) : AccountScreenEvent
    data class OnAddressQueryChange(val query: String) : AccountScreenEvent
    data class OnAddressSelected(val suggestion: AddressSuggestion) : AccountScreenEvent
    object OnClearAddress : AccountScreenEvent
    object OnSaveClick : AccountScreenEvent
    object OnLogoutClick : AccountScreenEvent
}

@HiltViewModel
class AccountScreenViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUserUseCase,
    private val updateAccountScreen: UpdateAccountScreen,
    private val geocodingService: GeocodingService
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountScreenState>(AccountScreenState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var searchJob: Job? = null

    init {
        loadUserProfile()
    }

    fun onEvent(event: AccountScreenEvent) {
        when (event) {
            is AccountScreenEvent.OnDescriptionChange -> {
                val currentState = _uiState.value
                if (currentState is AccountScreenState.Success) {
                    _uiState.value = currentState.copy(
                        userProfile = currentState.userProfile?.copy(
                            description = event.description
                        )
                    )
                }
            }

            is AccountScreenEvent.OnAddressQueryChange -> {
                val currentState = _uiState.value
                if (currentState is AccountScreenState.Success) {
                    _uiState.value = currentState.copy(
                        addressQuery = event.query,
                        selectedAddressDisplay = ""
                    )
                    searchAddressSuggestions(event.query)
                }
            }

            is AccountScreenEvent.OnAddressSelected -> {
                val currentState = _uiState.value
                if (currentState is AccountScreenState.Success) {
                    _uiState.value = currentState.copy(
                        userProfile = currentState.userProfile?.copy(
                            location = event.suggestion.geoPoint
                        ),
                        addressQuery = "",
                        addressSuggestions = emptyList(),
                        selectedAddressDisplay = event.suggestion.displayName
                    )
                }
            }

            is AccountScreenEvent.OnClearAddress -> {
                val currentState = _uiState.value
                if (currentState is AccountScreenState.Success) {
                    _uiState.value = currentState.copy(
                        userProfile = currentState.userProfile?.copy(location = null),
                        addressQuery = "",
                        addressSuggestions = emptyList(),
                        selectedAddressDisplay = ""
                    )
                }
            }

            is AccountScreenEvent.OnLogoutClick -> {
                logout()
            }

            is AccountScreenEvent.OnSaveClick -> {
                val currentState = _uiState.value
                if (currentState is AccountScreenState.Success) {
                    val userProfile = currentState.userProfile
                    if (userProfile != null) {
                        viewModelScope.launch {
                            _uiState.value = currentState.copy(isSaving = true)
                            updateAccountScreen(userProfile).collect { result ->
                                when (result) {
                                    is ApiResult.Success -> {
                                        _uiState.value = currentState.copy(isSaving = false)
                                        _eventFlow.emit(UiEvent.ShowSnackbar(result.message.toString()))
                                    }

                                    is ApiResult.Loading -> {
                                        _eventFlow.emit(UiEvent.ShowSnackbar("Updating..."))
                                    }

                                    is ApiResult.Error -> {
                                        _eventFlow.emit(UiEvent.ShowSnackbar(result.message.toString()))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun searchAddressSuggestions(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is AccountScreenState.Success) {
                if (query.length < 3) {
                    _uiState.value = currentState.copy(
                        addressSuggestions = emptyList(),
                        isLoadingSuggestions = false
                    )
                    return@launch
                }

                _uiState.value = currentState.copy(isLoadingSuggestions = true)
                delay(300) // Debounce

                val suggestions = geocodingService.getAddressSuggestions(query)
                val updatedState = _uiState.value
                if (updatedState is AccountScreenState.Success) {
                    _uiState.value = updatedState.copy(
                        addressSuggestions = suggestions,
                        isLoadingSuggestions = false
                    )
                }
            }
        }
    }

    private fun loadUserProfile() {
        getUserProfileUseCase().onEach { result ->
            when (result) {
                is ApiResult.Loading -> {
                    _uiState.value = AccountScreenState.Loading
                }

                is ApiResult.Success -> {
                    val profile = result.data
                    val displayAddress = profile?.location?.let {
                        geocodingService.getAddressFromGeoPoint(it)
                    } ?: ""

                    _uiState.value = AccountScreenState.Success(
                        userProfile = profile,
                        selectedAddressDisplay = displayAddress
                    )
                }

                is ApiResult.Error -> {
                    _uiState.value = AccountScreenState.Success(null)
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
                is ApiResult.Loading -> {
                    _uiState.value = AccountScreenState.Loading
                }

                is ApiResult.Success -> {
                    _navigationEvent.emit(NavigationEvent.NavigateToLogin)
                }

                is ApiResult.Error -> {
                    loadUserProfile()
                    _eventFlow.emit(
                        UiEvent.ShowSnackbar(
                            result.message ?: "Logout failed"
                        )
                    )
                }
            }
        }.launchIn(viewModelScope)
    }
}
