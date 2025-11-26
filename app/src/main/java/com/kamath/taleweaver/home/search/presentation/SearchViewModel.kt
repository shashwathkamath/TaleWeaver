package com.kamath.taleweaver.home.search.presentation

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.Constants.RADIUS_IN_KM
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.genres.domain.model.Genre
import com.kamath.taleweaver.genres.domain.usecase.GetGenresUseCase
import com.kamath.taleweaver.genres.domain.usecase.SyncGenresUseCase
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.search.domain.usecase.GetNearByBooksUseCase
import com.kamath.taleweaver.home.search.domain.usecase.SearchNearByBooksUseCase
import com.kamath.taleweaver.home.search.util.GeoFirestoreMigration
import com.kamath.taleweaver.home.search.util.LocationFacade
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


sealed interface SearchEvent {
    data class OnQueryChanged(val query: String) : SearchEvent
    object OnSearch : SearchEvent
    data class OnGenreToggle(val genreId: String) : SearchEvent
}

sealed interface SearchScreenState {
    object Loading : SearchScreenState
    data class Success(
        val listings: List<Listing> = emptyList(),
        val query: String = "",
        val availableGenres: List<Genre> = emptyList(),
        val selectedGenreIds: Set<String> = emptySet()
    ) : SearchScreenState

    data class Error(val message: String) : SearchScreenState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val locationFacade: LocationFacade,
    private val searchNearbyBooksUseCase: SearchNearByBooksUseCase,
    private val getNearbyBooksUseCase: GetNearByBooksUseCase,
    private val getGenresUseCase: GetGenresUseCase,
    private val syncGenresUseCase: SyncGenresUseCase,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchScreenState>(SearchScreenState.Success())
    val uiState = _uiState.asStateFlow()
    val hasLocationPermission: StateFlow<Boolean> = locationFacade.hasLocationPermission
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    private var fetchJob: Job? = null

    // Cache genres separately to avoid losing them during state transitions
    private var cachedGenres: List<Genre> = emptyList()

    init {
        loadGenres()
        checkPermission()
        migrateExistingListings()
        observePermissionChanges()
    }

    private fun loadGenres() {
        // Sync genres from Firestore if needed
        viewModelScope.launch {
            syncGenresUseCase()
        }

        // Observe genres from local database
        getGenresUseCase().onEach { genres ->
            Timber.d("Genres loaded from database: ${genres.size} genres")
            // Cache genres
            cachedGenres = genres

            val currentState = _uiState.value
            when (currentState) {
                is SearchScreenState.Success -> {
                    _uiState.value = currentState.copy(availableGenres = genres)
                }
                is SearchScreenState.Loading -> {
                    // Initialize to Success state with genres if currently loading
                    _uiState.value = SearchScreenState.Success(availableGenres = genres)
                }
                is SearchScreenState.Error -> {
                    // Keep error state but store genres for later
                    Timber.d("In error state, genres will be applied when state changes")
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.OnGenreToggle -> {
                Timber.d("Genre toggle event received for: ${event.genreId}")
                val currentState = _uiState.value
                if (currentState is SearchScreenState.Success) {
                    val currentSelected = currentState.selectedGenreIds
                    val newSelected = if (event.genreId in currentSelected) {
                        currentSelected - event.genreId
                    } else {
                        currentSelected + event.genreId
                    }
                    Timber.d("Selected genres updated from ${currentSelected} to $newSelected")
                    _uiState.value = currentState.copy(
                        selectedGenreIds = newSelected,
                        availableGenres = cachedGenres  // Preserve genres
                    )
                    // Reload search with new filter
                    getNearbyBooks()
                } else {
                    Timber.w("Cannot toggle genre: current state is not Success")
                }
            }
            else -> {
                // Handle other events if needed
            }
        }
    }


    private fun getNearbyBooks() {
        // Capture selected genres BEFORE changing state to Loading
        val currentState = _uiState.value
        val selectedGenres = if (currentState is SearchScreenState.Success) {
            currentState.selectedGenreIds
        } else {
            emptySet()
        }

        _uiState.value = SearchScreenState.Loading
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            locationFacade.getLastKnownLocation(
                applicationContext,
                onSuccess = { location ->
                    fetchAllBooksAtLocation(location, selectedGenres)
                },
                onFailure = { exception ->
                    _uiState.value = SearchScreenState.Error(
                        exception.message ?: Strings.Errors.LOCATION_FETCH_FAILED
                    )
                }
            )
        }
    }

    private fun fetchAllBooksAtLocation(location: Location, genreIds: Set<String>) {
        Timber.d("Fetching books with genres: $genreIds")

        getNearbyBooksUseCase(
            location.latitude,
            location.longitude,
            radiusInKm = RADIUS_IN_KM,
            genreIds = genreIds
        ).onEach { result ->
            val newState = when (result) {
                is ApiResult.Success -> {
                    // Always use cached genres to avoid losing them
                    SearchScreenState.Success(
                        listings = result.data ?: emptyList(),
                        query = "",
                        availableGenres = cachedGenres,
                        selectedGenreIds = genreIds
                    )
                }

                is ApiResult.Error -> {
                    SearchScreenState.Error(result.message ?: Strings.Errors.UNKNOWN)
                }

                is ApiResult.Loading -> {
                    SearchScreenState.Loading
                }
            }
            _uiState.value = newState
        }.launchIn(viewModelScope)
    }


    fun checkPermission() {
        locationFacade.checkPermissionStatus(applicationContext)
    }

    fun getFacade(): LocationFacade {
        return locationFacade
    }

    private fun observePermissionChanges() {
        viewModelScope.launch {
            hasLocationPermission.collect { hasLocationPermission ->
                if (hasLocationPermission) {
                    Timber.d("Location permission granted")
                    getNearbyBooks()
                }
            }
        }
    }

    /**
     * Migrate existing listings to GeoFirestore format.
     * Call this once to add geohash data to documents with plain GeoPoints.
     * After migration, your geo queries will work properly.
     */
    fun migrateExistingListings() {
        viewModelScope.launch {
            Timber.d("Starting GeoFirestore migration...")
            val result = GeoFirestoreMigration.migrateExistingListings(firestore)
            result.onSuccess { count ->
                Timber.d("Successfully migrated $count listings")
                // Refresh the search after migration
                getNearbyBooks()
            }.onFailure { error ->
                Timber.e(error, "Migration failed")
                _uiState.value = SearchScreenState.Error(
                    "${Strings.Errors.MIGRATION_FAILED}: ${error.message}"
                )
            }
        }
    }
}