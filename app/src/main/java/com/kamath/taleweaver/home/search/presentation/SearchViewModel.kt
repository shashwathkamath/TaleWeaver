package com.kamath.taleweaver.home.search.presentation

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.genres.domain.model.Genre
import com.kamath.taleweaver.genres.domain.model.GenreWithCount
import com.kamath.taleweaver.genres.domain.usecase.GetGenresUseCase
import com.kamath.taleweaver.genres.domain.usecase.SyncGenresUseCase
import com.kamath.taleweaver.genres.domain.util.GenreMatchHelper
import com.kamath.taleweaver.genres.domain.util.GenrePopularityHelper
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.search.domain.usecase.GetNearByBooksUseCase
import com.kamath.taleweaver.home.search.presentation.components.RadiusOption
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
        val genresWithCounts: List<GenreWithCount> = emptyList(),
        val selectedGenreId: String? = null,
        val selectedRadiusKm: Double = RadiusOption.LARGE.km,
        val isSearching: Boolean = false
    ) : SearchScreenState
    data class Error(val message: String) : SearchScreenState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val locationFacade: LocationFacade,
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

    private var cachedGenres: List<Genre> = emptyList()
    // Always the full set of books within MAX_FETCH_RADIUS_KM — never pre-filtered by genre
    private var allNearbyBooks: List<Listing> = emptyList()

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
        private const val MAX_FETCH_RADIUS_KM = 161.0  // ~100 miles — fetch once at max, filter locally
    }

    init {
        loadGenres()
        checkPermission()
        migrateExistingListings()
        observePermissionChanges()
    }

    private fun loadGenres() {
        viewModelScope.launch { syncGenresUseCase() }

        getGenresUseCase().onEach { genres ->
            cachedGenres = genres
            val currentState = _uiState.value
            if (currentState is SearchScreenState.Success) {
                _uiState.value = currentState.copy(availableGenres = genres)
            } else if (currentState is SearchScreenState.Loading) {
                _uiState.value = SearchScreenState.Success(availableGenres = genres)
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: SearchEvent) {
        val state = _uiState.value as? SearchScreenState.Success ?: return

        when (event) {
            is SearchEvent.OnQueryChanged -> {
                _uiState.value = state.copy(query = event.query, isSearching = true)
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(SEARCH_DEBOUNCE_MS)
                    applyAndEmitFilters(event.query, state.selectedGenreId, state.selectedRadiusKm)
                }
            }

            is SearchEvent.OnSearch -> {
                applyAndEmitFilters(state.query, state.selectedGenreId, state.selectedRadiusKm)
            }

            is SearchEvent.OnGenreToggle -> {
                val newSelected = if (event.genreId == state.selectedGenreId) null else event.genreId
                // Update selection immediately — no network call
                _uiState.value = state.copy(selectedGenreId = newSelected)
                applyAndEmitFilters(state.query, newSelected, state.selectedRadiusKm)
            }

            is SearchEvent.OnRadiusChanged -> {
                _uiState.value = state.copy(selectedRadiusKm = event.radiusKm)
                applyAndEmitFilters(state.query, state.selectedGenreId, event.radiusKm)
            }
        }
    }

    /**
     * Client-side filter over [allNearbyBooks]. No network call — instant.
     * Filters by: (1) user-selected radius, (2) text query, (3) genre.
     * Genre counts always reflect books within the selected radius regardless of genre filter.
     */
    private fun applyAndEmitFilters(query: String, selectedGenreId: String?, radiusKm: Double) {
        val currentState = _uiState.value as? SearchScreenState.Success ?: return

        val booksInRadius = allNearbyBooks.filter { it.distanceKm?.let { d -> d <= radiusKm } ?: false }

        val filteredListings = booksInRadius.filter { listing ->
            val matchesQuery = query.isBlank() ||
                listing.title.contains(query, ignoreCase = true) ||
                listing.author.contains(query, ignoreCase = true) ||
                listing.description.contains(query, ignoreCase = true)

            val matchesGenre = selectedGenreId == null || GenreMatchHelper.matchesGenres(
                bookGenres = listing.genres,
                selectedGenreIds = setOf(selectedGenreId),
                availableGenres = cachedGenres
            )

            matchesQuery && matchesGenre
        }.sortedBy { it.distanceKm }

        // Counts based on all books within radius — not affected by genre/text filter
        val genresWithCounts = GenrePopularityHelper.getGenresWithCounts(
            listings = booksInRadius,
            availableGenres = cachedGenres
        )

        _uiState.value = currentState.copy(
            listings = filteredListings,
            genresWithCounts = genresWithCounts,
            isSearching = false
        )
    }

    private fun getNearbyBooks() {
        val currentState = _uiState.value
        val selectedGenreId = (currentState as? SearchScreenState.Success)?.selectedGenreId
        val selectedRadiusKm = (currentState as? SearchScreenState.Success)?.selectedRadiusKm ?: RadiusOption.LARGE.km
        val currentQuery = (currentState as? SearchScreenState.Success)?.query ?: ""

        _uiState.value = SearchScreenState.Loading
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            locationFacade.getLastKnownLocation(
                applicationContext,
                onSuccess = { location ->
                    fetchAllBooksAtLocation(location, currentQuery, selectedGenreId, selectedRadiusKm)
                },
                onFailure = { exception ->
                    _uiState.value = SearchScreenState.Error(
                        exception.message ?: Strings.Errors.LOCATION_FETCH_FAILED
                    )
                }
            )
        }
    }

    /**
     * Fetches ALL books within [MAX_FETCH_RADIUS_KM] with no genre filter.
     * This is the only network call in search — everything else is client-side.
     */
    private fun fetchAllBooksAtLocation(
        location: Location,
        query: String,
        selectedGenreId: String?,
        selectedRadiusKm: Double
    ) {
        Timber.d("Fetching all books at MAX radius ($MAX_FETCH_RADIUS_KM km) — no genre filter")

        getNearbyBooksUseCase(
            location.latitude,
            location.longitude,
            radiusInKm = MAX_FETCH_RADIUS_KM,
            genreIds = emptySet()  // Never filter server-side — cache all, filter locally
        ).onEach { result ->
            when (result) {
                is ApiResult.Loading -> _uiState.value = SearchScreenState.Loading

                is ApiResult.Error -> {
                    _uiState.value = SearchScreenState.Error(result.message ?: Strings.Errors.UNKNOWN)
                }

                is ApiResult.Success -> {
                    allNearbyBooks = result.data ?: emptyList()
                    Timber.d("Cached ${allNearbyBooks.size} books at $MAX_FETCH_RADIUS_KM km")

                    // Compute filtered view for current query/genre/radius in one pass
                    val booksInRadius = allNearbyBooks.filter {
                        it.distanceKm?.let { d -> d <= selectedRadiusKm } ?: false
                    }
                    val filteredListings = booksInRadius.filter { listing ->
                        val matchesQuery = query.isBlank() ||
                            listing.title.contains(query, ignoreCase = true) ||
                            listing.author.contains(query, ignoreCase = true) ||
                            listing.description.contains(query, ignoreCase = true)
                        val matchesGenre = selectedGenreId == null || GenreMatchHelper.matchesGenres(
                            bookGenres = listing.genres,
                            selectedGenreIds = setOf(selectedGenreId),
                            availableGenres = cachedGenres
                        )
                        matchesQuery && matchesGenre
                    }.sortedBy { it.distanceKm }

                    val genresWithCounts = GenrePopularityHelper.getGenresWithCounts(
                        listings = booksInRadius,
                        availableGenres = cachedGenres
                    )

                    _uiState.value = SearchScreenState.Success(
                        listings = filteredListings,
                        query = query,
                        availableGenres = cachedGenres,
                        genresWithCounts = genresWithCounts,
                        selectedGenreId = selectedGenreId,
                        selectedRadiusKm = selectedRadiusKm,
                        isSearching = false
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun checkPermission() {
        locationFacade.checkPermissionStatus(applicationContext)
    }

    fun getFacade(): LocationFacade = locationFacade

    private fun observePermissionChanges() {
        viewModelScope.launch {
            hasLocationPermission.collect { hasPermission ->
                if (hasPermission && allNearbyBooks.isEmpty()) {
                    Timber.d("Location permission granted — fetching books for first time")
                    getNearbyBooks()
                }
            }
        }
    }

    fun migrateExistingListings() {
        viewModelScope.launch {
            Timber.d("Starting GeoFirestore migration...")
            val result = GeoFirestoreMigration.migrateExistingListings(firestore)
            result.onSuccess { count ->
                Timber.d("Successfully migrated $count listings")
                if (allNearbyBooks.isEmpty()) getNearbyBooks()
            }.onFailure { error ->
                Timber.e(error, "Migration failed")
                _uiState.value = SearchScreenState.Error(
                    "${Strings.Errors.MIGRATION_FAILED}: ${error.message}"
                )
            }
        }
    }
}
