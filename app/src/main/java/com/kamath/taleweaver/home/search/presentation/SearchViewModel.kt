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
import kotlinx.coroutines.delay
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
    data class OnRadiusChanged(val radiusKm: Double) : SearchEvent
}

sealed interface SearchScreenState {
    object Loading : SearchScreenState
    data class Success(
        val listings: List<Listing> = emptyList(),
        val query: String = "",
        val availableGenres: List<Genre> = emptyList(),
        val selectedGenreIds: Set<String> = emptySet(),
        val isSearching: Boolean = false,  // For debounced search loading indicator
        val radiusKm: Double = RADIUS_IN_KM  // Current search radius
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
    private var searchJob: Job? = null

    // Cache genres separately to avoid losing them during state transitions
    private var cachedGenres: List<Genre> = emptyList()

    // Cache all nearby books for client-side filtering
    private var allNearbyBooks: List<Listing> = emptyList()

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L  // Debounce delay for search
        private const val MAX_FETCH_RADIUS_KM = 50.0  // Always fetch at max radius, filter client-side
    }

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
            is SearchEvent.OnQueryChanged -> {
                val currentState = _uiState.value
                if (currentState is SearchScreenState.Success) {
                    // Update query immediately (for UI responsiveness)
                    _uiState.value = currentState.copy(
                        query = event.query,
                        isSearching = true
                    )
                    // Debounced search
                    performDebouncedSearch(event.query, currentState.selectedGenreIds, currentState.radiusKm)
                }
            }

            is SearchEvent.OnSearch -> {
                val currentState = _uiState.value
                if (currentState is SearchScreenState.Success) {
                    performSearch(currentState.query, currentState.selectedGenreIds, currentState.radiusKm)
                }
            }

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

            is SearchEvent.OnRadiusChanged -> {
                Timber.d("Radius changed to: ${event.radiusKm} km")
                val currentState = _uiState.value
                if (currentState is SearchScreenState.Success) {
                    _uiState.value = currentState.copy(
                        radiusKm = event.radiusKm,
                        availableGenres = cachedGenres  // Preserve genres
                    )
                    // Apply radius filter client-side (no Firestore query)
                    performSearch(currentState.query, currentState.selectedGenreIds, event.radiusKm)
                } else {
                    Timber.w("Cannot change radius: current state is not Success")
                }
            }
        }
    }

    /**
     * Debounced search - waits for user to stop typing before searching
     * This prevents excessive API calls and improves performance
     */
    private fun performDebouncedSearch(query: String, selectedGenres: Set<String>, radiusKm: Double) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            performSearch(query, selectedGenres, radiusKm)
        }
    }

    /**
     * Perform search with client-side filtering for better performance
     * Since we already have nearby books cached (at max radius), filter them by:
     * - Selected radius (distance)
     * - Text query
     * - Genres
     * No network call needed!
     */
    private fun performSearch(query: String, selectedGenres: Set<String>, radiusKm: Double) {
        val currentState = _uiState.value
        if (currentState !is SearchScreenState.Success) return

        // Client-side filtering (instant, no network call needed)
        val filteredListings = allNearbyBooks.filter { listing ->
            // 1. Filter by radius (distance)
            val withinRadius = listing.distanceKm?.let { it <= radiusKm } ?: false

            // 2. Filter by text query
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                listing.title.contains(query, ignoreCase = true) ||
                listing.author.contains(query, ignoreCase = true) ||
                listing.description.contains(query, ignoreCase = true)
            }

            // 3. Filter by genre
            val matchesGenre = if (selectedGenres.isEmpty()) {
                true
            } else {
                val enumNames = selectedGenres.map { it.uppercase().replace("-", "_") }
                listing.genres.any { listingGenre ->
                    enumNames.contains(listingGenre.name)
                }
            }

            withinRadius && matchesQuery && matchesGenre
        }

        _uiState.value = currentState.copy(
            listings = filteredListings.sortedBy { it.distanceKm },
            isSearching = false
        )
    }


    private fun getNearbyBooks() {
        // Capture selected genres and UI radius BEFORE changing state to Loading
        val currentState = _uiState.value
        val selectedGenres = if (currentState is SearchScreenState.Success) {
            currentState.selectedGenreIds
        } else {
            emptySet()
        }
        val uiRadiusKm = if (currentState is SearchScreenState.Success) {
            currentState.radiusKm
        } else {
            RADIUS_IN_KM
        }

        _uiState.value = SearchScreenState.Loading
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            locationFacade.getLastKnownLocation(
                applicationContext,
                onSuccess = { location ->
                    // Always fetch at MAX radius, filter client-side by uiRadiusKm
                    fetchAllBooksAtLocation(location, selectedGenres, uiRadiusKm)
                },
                onFailure = { exception ->
                    _uiState.value = SearchScreenState.Error(
                        exception.message ?: Strings.Errors.LOCATION_FETCH_FAILED
                    )
                }
            )
        }
    }

    private fun fetchAllBooksAtLocation(location: Location, genreIds: Set<String>, uiRadiusKm: Double) {
        Timber.d("Fetching books at MAX radius (${MAX_FETCH_RADIUS_KM} km), will filter to ${uiRadiusKm} km client-side")

        getNearbyBooksUseCase(
            location.latitude,
            location.longitude,
            radiusInKm = MAX_FETCH_RADIUS_KM,  // Always fetch at max radius
            genreIds = genreIds
        ).onEach { result ->
            val newState = when (result) {
                is ApiResult.Success -> {
                    val allListings = result.data ?: emptyList()
                    // Cache ALL books (at max radius) for client-side filtering
                    allNearbyBooks = allListings
                    Timber.d("Cached ${allNearbyBooks.size} books at ${MAX_FETCH_RADIUS_KM} km radius")

                    // Filter to UI radius client-side
                    val filteredByRadius = allListings.filter { listing ->
                        listing.distanceKm?.let { it <= uiRadiusKm } ?: false
                    }.sortedBy { it.distanceKm }

                    Timber.d("Displaying ${filteredByRadius.size} books within ${uiRadiusKm} km")

                    // Always use cached genres to avoid losing them
                    SearchScreenState.Success(
                        listings = filteredByRadius,
                        query = "",
                        availableGenres = cachedGenres,
                        selectedGenreIds = genreIds,
                        isSearching = false,
                        radiusKm = uiRadiusKm
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