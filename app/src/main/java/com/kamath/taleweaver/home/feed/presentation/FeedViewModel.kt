package com.kamath.taleweaver.home.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.feed.domain.model.Tale
import com.kamath.taleweaver.home.feed.domain.usecase.GetAllFeed
import com.kamath.taleweaver.home.feed.domain.usecase.GetMoreFeed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class FeedScreenState(
    val tales: List<Tale> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val lastVisibleTale: DocumentSnapshot? = null
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getAllFeed: GetAllFeed,
    private val getMoreFeed: GetMoreFeed
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialFeed()
    }

    private fun loadInitialFeed() {
        getAllFeed().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true
                    )
                }

                is Resource.Success -> {
                    val snapshot = result.data!!
                    val newTales = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Tale::class.java)?.copy(
                            id = doc.id
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        tales = newTales,
                        lastVisibleTale = snapshot.documents.lastOrNull(),
                        endReached = snapshot.isEmpty
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message ?: "An Unknown error occurred"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun loadMoreTales() {
        if (_uiState.value.isLoadingMore || _uiState.value.endReached) {
            return
        }

        val lastVisible = _uiState.value.lastVisibleTale ?: return

        getMoreFeed(lastVisible).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = true,
                        error = null
                    )
                }

                is Resource.Success -> {
                    val snapshot = result.data!!
                    val newTales = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Tale::class.java)?.copy(
                            id = doc.id
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        tales = _uiState.value.tales + newTales,
                        lastVisibleTale = snapshot.documents.lastOrNull(),
                        endReached = newTales.isEmpty()
                    )
                }

                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        error = result.message ?: "Could not load more tales"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }
}