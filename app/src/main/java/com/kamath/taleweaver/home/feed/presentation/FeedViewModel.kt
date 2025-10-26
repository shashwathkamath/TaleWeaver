package com.kamath.taleweaver.home.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.kamath.taleweaver.core.util.FirebaseDiagnostics
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.feed.domain.model.Tale
import com.kamath.taleweaver.home.feed.domain.usecase.GetAllFeed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val getInitialFeed: GetAllFeed,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialFeed()
        viewModelScope.launch {
            val collections = FirebaseDiagnostics.listRootCollections()
            if (collections.isNotEmpty()) {
                FirebaseDiagnostics.sampleCollections(collections)
            }
        }
    }

    fun onEvent(event: FeedEvent) {
        when (event) {
            is FeedEvent.Refresh -> loadInitialFeed()
            // We'll ignore LoadMoreTales for now
            else -> {}
        }
    }

    private fun loadInitialFeed() {
        // Launch a coroutine to call the suspend function
        viewModelScope.launch {
            getInitialFeed().onEach { result ->
                _uiState.update { currentState -> // Use .update for thread-safe state changes
                    when (result) {
                        is Resource.Loading -> {
                            // On refresh, clear old data while loading
                            currentState.copy(isLoading = true, error = null, tales = emptyList())
                        }

                        is Resource.Success -> {
                            val snapshot = result.data!!
                            val newTales =
                                snapshot.toObjects(Tale::class.java).mapIndexed { index, tale ->
                                    tale.copy(id = snapshot.documents[index].id)
                                }
                            currentState.copy(
                                isLoading = false,
                                tales = newTales,
                                lastVisibleTale = snapshot.documents.lastOrNull(),
                                endReached = snapshot.isEmpty
                            )
                        }

                        is Resource.Error -> {
                            currentState.copy(
                                isLoading = false,
                                error = result.message ?: "An unknown error occurred"
                            )
                        }
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
}

// Sealed interface for UI events for better structure
sealed interface FeedEvent {
    // object LoadMoreTales : FeedEvent // Commented out for now
    object Refresh : FeedEvent
}
