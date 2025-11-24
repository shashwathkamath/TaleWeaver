package com.kamath.taleweaver.home.account.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.core.util.UiEvent
import com.kamath.taleweaver.home.account.domain.usecase.DeleteListingUseCase
import com.kamath.taleweaver.home.account.domain.usecase.GetUserListingsUseCase
import com.kamath.taleweaver.home.feed.domain.model.Listing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MyListingsUiState {
    object Loading : MyListingsUiState
    data class Success(val listings: List<Listing>) : MyListingsUiState
    data class Error(val message: String) : MyListingsUiState
}

@HiltViewModel
class MyListingsViewModel @Inject constructor(
    private val getUserListingsUseCase: GetUserListingsUseCase,
    private val deleteListingUseCase: DeleteListingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyListingsUiState>(MyListingsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadListings()
    }

    private fun loadListings() {
        viewModelScope.launch {
            getUserListingsUseCase().collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.value = MyListingsUiState.Loading
                    }
                    is ApiResult.Success -> {
                        _uiState.value = MyListingsUiState.Success(result.data ?: emptyList())
                    }
                    is ApiResult.Error -> {
                        _uiState.value = MyListingsUiState.Error(result.message ?: Strings.Errors.UNKNOWN)
                    }
                }
            }
        }
    }

    fun deleteListing(listingId: String) {
        viewModelScope.launch {
            deleteListingUseCase(listingId).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        // Optionally show loading indicator
                    }
                    is ApiResult.Success -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar(Strings.Success.LISTING_DELETED))
                        loadListings() // Refresh the list
                    }
                    is ApiResult.Error -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: Strings.Errors.DELETE_FAILED))
                    }
                }
            }
        }
    }
}
