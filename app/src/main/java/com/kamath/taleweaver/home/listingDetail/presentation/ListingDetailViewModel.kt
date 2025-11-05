package com.kamath.taleweaver.home.listingDetail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamath.taleweaver.core.navigation.AppDestination
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.listingDetail.domain.usecase.GetListingById
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ListingDetailState(
    val listing: Listing? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val getListingById: GetListingById,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(ListingDetailState())
    val uiState = _uiState.asStateFlow()

    init {
        // 3. Get the listing ID from navigation arguments
        savedStateHandle.get<String>(AppDestination.ARG_LISTING_ID)?.let { listingId ->
            if (listingId.isNotEmpty()) {
                fetchListingDetails(listingId)
            }
        }
    }

    private fun fetchListingDetails(listingId: String) {
        getListingById(listingId).onEach { result ->
            _uiState.update { currentState ->
                when (result) {
                    is Resource.Loading -> {
                        currentState.copy(isLoading = true)
                    }

                    is Resource.Success -> {
                        currentState.copy(isLoading = false, listing = result.data, error = null)
                    }

                    is Resource.Error -> {
                        currentState.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }
}