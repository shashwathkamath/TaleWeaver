package com.kamath.taleweaver.home.account.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.navigation.NavigationEvent
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.core.util.UiEvent
import com.kamath.taleweaver.home.account.domain.usecase.GetUserListingsUseCase
import com.kamath.taleweaver.home.account.domain.usecase.GetUserProfileUseCase
import com.kamath.taleweaver.home.account.domain.usecase.LogoutUserUseCase
import com.kamath.taleweaver.home.account.domain.usecase.UpdateAccountScreen
import com.kamath.taleweaver.home.account.domain.usecase.UploadProfilePictureUseCase
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.order.domain.model.Order
import com.kamath.taleweaver.order.domain.usecase.GetUserPurchasesUseCase
import com.kamath.taleweaver.order.domain.usecase.GetUserSalesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class AccountTab {
    PROFILE_INFO,
    MY_LISTINGS,
    SHIPMENT
}

sealed interface AccountScreenState {
    object Loading : AccountScreenState
    data class Success(
        val userProfile: UserProfile?,
        val originalProfile: UserProfile? = null,  // Track original to detect changes
        val isSaving: Boolean = false,
        val isUploadingPhoto: Boolean = false,
        val myListings: List<Listing> = emptyList(),
        val isLoadingListings: Boolean = false,
        val myPurchases: List<Order> = emptyList(),  // Books I bought
        val mySales: List<Order> = emptyList(),      // Books I'm selling
        val isLoadingOrders: Boolean = false,
        val selectedTab: AccountTab = AccountTab.PROFILE_INFO
    ) : AccountScreenState {
        val hasUnsavedChanges: Boolean
            get() = userProfile != null && originalProfile != null &&
                    (userProfile.description != originalProfile.description ||
                     userProfile.address != originalProfile.address)
    }

    data class Error(val message: String) : AccountScreenState
}

sealed interface AccountScreenEvent {
    data class OnDescriptionChange(val description: String) : AccountScreenEvent
    data class OnAddressChange(val address: String) : AccountScreenEvent
    data class OnProfilePhotoSelected(val uri: Uri) : AccountScreenEvent
    data class OnTabSelected(val tab: AccountTab) : AccountScreenEvent
    object OnSaveClick : AccountScreenEvent
    object OnLogoutClick : AccountScreenEvent
}

@HiltViewModel
class AccountScreenViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserListingsUseCase: GetUserListingsUseCase,
    private val getUserPurchasesUseCase: GetUserPurchasesUseCase,
    private val getUserSalesUseCase: GetUserSalesUseCase,
    private val logoutUseCase: LogoutUserUseCase,
    private val updateAccountScreen: UpdateAccountScreen,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AccountScreenState>(AccountScreenState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadUserProfile()
        loadUserListings()
        loadUserOrders()
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

            is AccountScreenEvent.OnAddressChange -> {
                val currentState = _uiState.value
                if (currentState is AccountScreenState.Success) {
                    _uiState.value = currentState.copy(
                        userProfile = currentState.userProfile?.copy(
                            address = event.address
                        )
                    )
                }
            }

            is AccountScreenEvent.OnProfilePhotoSelected -> {
                uploadProfilePicture(event.uri)
            }

            is AccountScreenEvent.OnTabSelected -> {
                val currentState = _uiState.value
                if (currentState is AccountScreenState.Success) {
                    _uiState.value = currentState.copy(selectedTab = event.tab)
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
                                        // Reset originalProfile to current after successful save
                                        _uiState.value = currentState.copy(
                                            isSaving = false,
                                            originalProfile = userProfile
                                        )
                                        _eventFlow.emit(UiEvent.ShowSnackbar(Strings.Success.PROFILE_SAVED))
                                    }

                                    is ApiResult.Loading -> {
                                        // Don't show snackbar for loading state
                                    }

                                    is ApiResult.Error -> {
                                        _uiState.value = currentState.copy(isSaving = false)
                                        _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: Strings.Errors.UNKNOWN))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun uploadProfilePicture(uri: Uri) {
        viewModelScope.launch {
            uploadProfilePictureUseCase(uri).collect { result ->
                val currentState = _uiState.value
                when (result) {
                    is ApiResult.Loading -> {
                        if (currentState is AccountScreenState.Success) {
                            _uiState.value = currentState.copy(isUploadingPhoto = true)
                        }
                    }

                    is ApiResult.Success -> {
                        if (currentState is AccountScreenState.Success) {
                            val updatedProfile = currentState.userProfile?.copy(
                                profilePictureUrl = result.data ?: ""
                            )
                            _uiState.value = currentState.copy(
                                isUploadingPhoto = false,
                                userProfile = updatedProfile,
                                originalProfile = updatedProfile
                            )
                        }
                        _eventFlow.emit(UiEvent.ShowSnackbar(Strings.Success.PHOTO_UPDATED))
                    }

                    is ApiResult.Error -> {
                        if (currentState is AccountScreenState.Success) {
                            _uiState.value = currentState.copy(isUploadingPhoto = false)
                        }
                        _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: Strings.Errors.PHOTO_UPLOAD_FAILED))
                    }
                }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            getUserProfileUseCase().collect { result ->
                val currentState = _uiState.value
                when (result) {
                    is ApiResult.Loading -> {
                        if (currentState !is AccountScreenState.Success) {
                            _uiState.value = AccountScreenState.Loading
                        }
                    }

                    is ApiResult.Success -> {
                        // Preserve existing listings data when updating profile
                        val existingListings = (currentState as? AccountScreenState.Success)?.myListings ?: emptyList()
                        val isLoadingListings = (currentState as? AccountScreenState.Success)?.isLoadingListings ?: true
                        _uiState.value = AccountScreenState.Success(
                            userProfile = result.data,
                            originalProfile = result.data,  // Set original for dirty tracking
                            myListings = existingListings,
                            isLoadingListings = isLoadingListings
                        )
                    }

                    is ApiResult.Error -> {
                        val existingListings = (currentState as? AccountScreenState.Success)?.myListings ?: emptyList()
                        _uiState.value = AccountScreenState.Success(
                            userProfile = null,
                            myListings = existingListings,
                            isLoadingListings = false
                        )
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                result.message ?: Strings.Errors.UNKNOWN
                            )
                        )
                    }
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            logoutUseCase().collect { result ->
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
                                result.message ?: Strings.Errors.LOGOUT_FAILED
                            )
                        )
                    }
                }
            }
        }
    }

    private fun loadUserListings() {
        viewModelScope.launch {
            getUserListingsUseCase().collect { result ->
                val currentState = _uiState.value
                when (result) {
                    is ApiResult.Loading -> {
                        if (currentState is AccountScreenState.Success) {
                            _uiState.value = currentState.copy(isLoadingListings = true)
                        }
                        // If not yet Success, the isLoadingListings = true default will apply
                    }

                    is ApiResult.Success -> {
                        val listings = result.data ?: emptyList()
                        if (currentState is AccountScreenState.Success) {
                            _uiState.value = currentState.copy(
                                myListings = listings,
                                isLoadingListings = false
                            )
                        } else {
                            // Profile hasn't loaded yet, create a new Success state
                            _uiState.value = AccountScreenState.Success(
                                userProfile = null,
                                myListings = listings,
                                isLoadingListings = false
                            )
                        }
                    }

                    is ApiResult.Error -> {
                        if (currentState is AccountScreenState.Success) {
                            _uiState.value = currentState.copy(isLoadingListings = false)
                        }
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(result.message ?: "Failed to load listings")
                        )
                    }
                }
            }
        }
    }

    private fun loadUserOrders() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val userId = (currentState as? AccountScreenState.Success)?.userProfile?.userId

            if (userId == null) {
                // Wait for profile to load
                getUserProfileUseCase().collect { profileResult ->
                    if (profileResult is ApiResult.Success) {
                        profileResult.data?.userId?.let { id ->
                            fetchOrders(id)
                        }
                    }
                }
            } else {
                fetchOrders(userId)
            }
        }
    }

    private suspend fun fetchOrders(userId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is AccountScreenState.Success) {
                _uiState.value = currentState.copy(isLoadingOrders = true)
            }

            // Fetch purchases
            val purchasesResult = getUserPurchasesUseCase(userId)
            val salesResult = getUserSalesUseCase(userId)

            val purchases = purchasesResult.getOrNull() ?: emptyList()
            val sales = salesResult.getOrNull() ?: emptyList()

            val state = _uiState.value
            if (state is AccountScreenState.Success) {
                _uiState.value = state.copy(
                    myPurchases = purchases,
                    mySales = sales,
                    isLoadingOrders = false
                )
            }
        }
    }
}
