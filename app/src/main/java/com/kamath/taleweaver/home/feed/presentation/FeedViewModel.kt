package com.kamath.taleweaver.home.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.FirebaseDiagnostics
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.genres.domain.model.Genre
import com.kamath.taleweaver.genres.domain.usecase.GetGenresUseCase
import com.kamath.taleweaver.genres.domain.usecase.SyncGenresUseCase
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.feed.domain.usecase.GetAllFeed
import com.kamath.taleweaver.home.feed.domain.usecase.GetMoreFeed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


data class FeedScreenState(
    val listings: List<Listing> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val lastVisibleTale: DocumentSnapshot? = null,
    val currentUserId: String? = null,
    val availableGenres: List<Genre> = emptyList(),
    val selectedGenreId: String? = null  // Changed from Set to nullable String for single selection
)


sealed interface FeedEvent {
    object Refresh : FeedEvent
    object LoadMore : FeedEvent
    data class OnGenreToggle(val genreId: String) : FeedEvent
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getInitialFeed: GetAllFeed,
    private val getMoreFeed: GetMoreFeed,
    private val getGenresUseCase: GetGenresUseCase,
    private val syncGenresUseCase: SyncGenresUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedScreenState(
        currentUserId = auth.currentUser?.uid
    ))
    val uiState = _uiState.asStateFlow()

    init {
        loadGenres()
        // Note: Feed is now loaded when FeedScreen composable enters composition
        // This prevents unnecessary data fetching on app start
        viewModelScope.launch {
            val collections = FirebaseDiagnostics.listRootCollections()
            if (collections.isNotEmpty()) {
                FirebaseDiagnostics.sampleCollections(collections)
            }
        }
    }

    private fun loadGenres() {
        // Sync genres from Firestore if needed
        viewModelScope.launch {
            syncGenresUseCase()
        }

        // Observe genres from local database
        getGenresUseCase().onEach { genres ->
            _uiState.update { it.copy(availableGenres = genres) }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: FeedEvent) {
        when (event) {
            is FeedEvent.Refresh -> refreshFeed()
            is FeedEvent.LoadMore -> loadMoreFeed()
            is FeedEvent.OnGenreToggle -> {
                val currentSelected = _uiState.value.selectedGenreId
                // Toggle logic: if same genre clicked, deselect it; otherwise select the new genre
                val newSelected = if (event.genreId == currentSelected) {
                    null  // Deselect if clicking the same genre
                } else {
                    event.genreId  // Select the new genre
                }
                _uiState.update { it.copy(selectedGenreId = newSelected) }
                // Reload feed with new filter
                refreshFeed()
            }
        }
    }

    fun refreshFeed() {
        val genreId = _uiState.value.selectedGenreId
        val genreIds = if (genreId != null) setOf(genreId) else emptySet()
        getInitialFeed(genreIds).onEach { result ->
            _uiState.update { currentState ->
                when (result) {
                    is ApiResult.Loading -> {
                        currentState.copy(isLoading = true, error = null, listings = emptyList())
                    }

                    is ApiResult.Success -> {
                        val snapshot = result.data!!
                        val newListings =
                            snapshot.toObjects(Listing::class.java).mapIndexed { index, listing ->
                                listing.copy(id = snapshot.documents[index].id)
                            }
                        currentState.copy(
                            isLoading = false,
                            listings = newListings,
                            lastVisibleTale = snapshot.documents.lastOrNull(),
                            endReached = snapshot.isEmpty
                        )
                    }

                    is ApiResult.Error -> {
                        currentState.copy(
                            isLoading = false,
                            error = result.message ?: Strings.Errors.UNKNOWN
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun loadMoreFeed() {
        Timber.d("Load more")
        val currentState = _uiState.value
        val lastVisible = currentState.lastVisibleTale
        if (currentState.isLoadingMore || currentState.endReached || lastVisible == null) {
            return
        }
        val genreId = currentState.selectedGenreId
        val genreIds = if (genreId != null) setOf(genreId) else emptySet()
        getMoreFeed(lastVisible, genreIds).onEach { result ->
            _uiState.update { state ->
                when (result) {
                    is ApiResult.Loading -> state.copy(isLoadingMore = true)
                    is ApiResult.Success -> {
                        val snapshot = result.data!!
                        val moreListings =
                            snapshot.toObjects(Listing::class.java).mapIndexed { index, tale ->
                                tale.copy(id = snapshot.documents[index].id)
                            }
                        state.copy(
                            isLoadingMore = false,
                            listings = state.listings + moreListings,
                            lastVisibleTale = snapshot.documents.lastOrNull(),
                            endReached = snapshot.isEmpty
                        )
                    }

                    is ApiResult.Error -> {
                        state.copy(
                            isLoadingMore = false,
                            error = result.message ?: Strings.Errors.UNKNOWN
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }
}

