package com.kamath.taleweaver.home.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.util.FirebaseDiagnostics
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.feed.domain.usecase.GetAllFeed
import com.kamath.taleweaver.home.feed.domain.usecase.GetMoreFeed
import com.kamath.taleweaver.home.feed.utils.FirestoreSeeder
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
    val currentUserId: String? = null
)


sealed interface FeedEvent {
    object Refresh : FeedEvent
    object LoadMore : FeedEvent
    object SeedDatabase : FeedEvent
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getInitialFeed: GetAllFeed,
    private val getMoreFeed: GetMoreFeed,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedScreenState(
        currentUserId = auth.currentUser?.uid
    ))
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
            is FeedEvent.LoadMore -> loadMoreFeed()
            is FeedEvent.SeedDatabase -> seedDatabase()
        }
    }

    private fun loadInitialFeed() {
        getInitialFeed().onEach { result ->
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
        getMoreFeed(lastVisible).onEach { result ->
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

    private fun seedDatabase() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Call your centralized seeder
                FirestoreSeeder.seedDatabase(firestore)
                //_uiState.update { it.copy(isLoading = false) }
                // Refresh the feed to show the new data
                //loadInitialFeed()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "${Strings.Errors.SEED_DATABASE_FAILED}: ${e.message}"
                    )
                }
            }
        }
    }
}

