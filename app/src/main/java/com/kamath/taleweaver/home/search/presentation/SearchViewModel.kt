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
import com.kamath.taleweaver.genres.domain.model.GenreWithCount
import com.kamath.taleweaver.genres.domain.usecase.GetGenresUseCase
import com.kamath.taleweaver.genres.domain.usecase.SyncGenresUseCase
import com.kamath.taleweaver.genres.domain.util.GenreMatchHelper
import com.kamath.taleweaver.genres.domain.util.GenrePopularityHelper
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
}

sealed interface SearchScreenState {
    object Loading : SearchScreenState
    data class Success(
        val listings: List<Listing> = emptyList(),
        val query: String = "",
        val availableGenres: List<Genre> = emptyList(),
        val genresWithCounts: List<GenreWithCount> = emptyList(),
        val selectedGenreId: String? = null,  // Changed from Set to nullable String for single selection
        val isSearching: Boolean = false  // For debounced search loading indicator
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
        private const val MAX_FETCH_RADIUS_KM = 161.0  // 100 miles, always fetch at max radius
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
                    val genreIds = if (currentState.selectedGenreId != null) setOf(currentState.selectedGenreId) else emptySet()
                    performDebouncedSearch(event.query, genreIds)
                }
            }

            is SearchEvent.OnSearch -> {
                val currentState = _uiState.value
                if (currentState is SearchScreenState.Success) {
                    val genreIds = if (currentState.selectedGenreId != null) setOf(currentState.selectedGenreId) else emptySet()
                    performSearch(currentState.query, genreIds)
                }
            }

            is SearchEvent.OnGenreToggle -> {
                Timber.d("Genre toggle event received for: ${event.genreId}")
                val currentState = _uiState.value
                if (currentState is SearchScreenState.Success) {
                    val currentSelected = currentState.selectedGenreId
                    // Toggle logic: if same genre clicked, deselect it; otherwise select the new genre
                    val newSelected = if (event.genreId == currentSelected) {
                        null  // Deselect if clicking the same genre
                    } else {
                        event.genreId  // Select the new genre
                    }
                    Timber.d("Selected genre updated from ${currentSelected} to $newSelected")
                    _uiState.value = currentState.copy(
                        selectedGenreId = newSelected,
                        availableGenres = cachedGenres  // Preserve genres
                    )
                    // Reload search with new filter
                    getNearbyBooks()
                } else {
                    Timber.w("Cannot toggle genre: current state is not Success")
                }
            }
        }
    }

    /**
     * Debounced search - waits for user to stop typing before searching
     * This prevents excessive API calls and improves performance
     */
    private fun performDebouncedSearch(query: String, selectedGenres: Set<String>) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            performSearch(query, selectedGenres)
        }
    }

    /**
     * Perform search with client-side filtering for better performance
     * Since we already have nearby books cached (at max radius), filter them by:
     * - Text query
     * - Genres
     * No network call needed!
     */
    private fun performSearch(query: String, selectedGenres: Set<String>) {
        val currentState = _uiState.value
        if (currentState !is SearchScreenState.Success) return

        // Client-side filtering (instant, no network call needed)
        val filteredListings = allNearbyBooks.filter { listing ->
            // 1. Filter by radius (distance) - always use max radius
            val withinRadius = listing.distanceKm?.let { it <= MAX_FETCH_RADIUS_KM } ?: false

            // 2. Filter by text query
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                listing.title.contains(query, ignoreCase = true) ||
                listing.author.contains(query, ignoreCase = true) ||
                listing.description.contains(query, ignoreCase = true)
            }

            // 3. Filter by genre (with contextual matching)
            val matchesGenre = if (selectedGenres.isEmpty()) {
                true
            } else {
                GenreMatchHelper.matchesGenres(
                    bookGenres = listing.genres,
                    selectedGenreIds = selectedGenres,
                    availableGenres = cachedGenres
                )
            }

            withinRadius && matchesQuery && matchesGenre
        }

        // Calculate genre counts from all nearby books (not just filtered ones)
        val genresWithCounts = GenrePopularityHelper.getGenresWithCounts(
            listings = allNearbyBooks,
            availableGenres = cachedGenres
        )

        _uiState.value = currentState.copy(
            listings = filteredListings.sortedBy { it.distanceKm },
            genresWithCounts = genresWithCounts,
            isSearching = false
        )
    }


    private fun getNearbyBooks() {
        // Capture selected genre BEFORE changing state to Loading
        val currentState = _uiState.value
        val selectedGenreId = if (currentState is SearchScreenState.Success) {
            currentState.selectedGenreId
        } else {
            null
        }
        val selectedGenres = if (selectedGenreId != null) setOf(selectedGenreId) else emptySet()

        _uiState.value = SearchScreenState.Loading
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            locationFacade.getLastKnownLocation(
                applicationContext,
                onSuccess = { location ->
                    // Always fetch at MAX radius
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
        Timber.d("Fetching books at MAX radius (${MAX_FETCH_RADIUS_KM} km)")

        // Expand genre IDs to include all contextually matching genres for backend query
        val expandedGenreEnums = if (genreIds.isNotEmpty()) {
            GenreMatchHelper.expandGenresToEnumNames(genreIds, cachedGenres)
        } else {
            emptySet()
        }
        Timber.d("Genre filter: selected=$genreIds, expanded enums=$expandedGenreEnums")

        getNearbyBooksUseCase(
            location.latitude,
            location.longitude,
            radiusInKm = MAX_FETCH_RADIUS_KM,  // Always fetch at max radius
            genreIds = expandedGenreEnums
        ).onEach { result ->
            val newState = when (result) {
                is ApiResult.Success -> {
                    val allListings = result.data ?: emptyList()
                    // Cache ALL books (at max radius) for client-side filtering
                    allNearbyBooks = allListings
                    Timber.d("Cached ${allNearbyBooks.size} books at ${MAX_FETCH_RADIUS_KM} km radius")

                    // Sort by distance
                    val sortedListings = allListings.sortedBy { it.distanceKm }

                    Timber.d("Displaying ${sortedListings.size} books within ${MAX_FETCH_RADIUS_KM} km")

                    // Calculate genre counts from all cached books
                    val genresWithCounts = GenrePopularityHelper.getGenresWithCounts(
                        listings = allListings,
                        availableGenres = cachedGenres
                    )

                    // Always use cached genres to avoid losing them
                    SearchScreenState.Success(
                        listings = sortedListings,
                        query = "",
                        availableGenres = cachedGenres,
                        genresWithCounts = genresWithCounts,
                        selectedGenreId = genreIds.firstOrNull(),  // Convert Set back to nullable String
                        isSearching = false
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