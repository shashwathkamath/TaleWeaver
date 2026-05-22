package com.taleweaver.app.home.search.presentation

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.core.util.Strings
import com.taleweaver.app.genres.domain.model.Genre
import com.taleweaver.app.genres.domain.model.GenreWithCount
import com.taleweaver.app.genres.domain.usecase.GetGenresUseCase
import com.taleweaver.app.genres.domain.usecase.SyncGenresUseCase
import com.taleweaver.app.genres.domain.util.GenreMatchHelper
import com.taleweaver.app.home.feed.domain.model.Listing
import com.taleweaver.app.home.search.domain.usecase.GetNearByBooksUseCase
import com.taleweaver.app.home.search.presentation.components.RadiusOption
import com.taleweaver.app.home.search.util.LocationFacade
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
    object OnLoadMore : SearchEvent
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
        val isSearching: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMorePages: Boolean = false,
        val currentPage: Int = 0
    ) : SearchScreenState
    data class Error(val message: String) : SearchScreenState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val locationFacade: LocationFacade,
    private val getNearbyBooksUseCase: GetNearByBooksUseCase,
    private val getGenresUseCase: GetGenresUseCase,
    private val syncGenresUseCase: SyncGenresUseCase,
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

    private var searchJob: Job? = null
    private var cachedGenres: List<Genre> = emptyList()
    private var currentLocation: Location? = null

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }

    init {
        loadGenres()
        checkPermission()
        observePermissionChanges()
    }

    private fun loadGenres() {
        viewModelScope.launch { syncGenresUseCase() }

        getGenresUseCase().onEach { genres ->
            cachedGenres = genres
            val state = _uiState.value
            if (state is SearchScreenState.Success) {
                _uiState.value = state.copy(availableGenres = genres)
            } else if (state is SearchScreenState.Loading) {
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
                    triggerSearch(
                        query = event.query,
                        genreId = state.selectedGenreId,
                        radiusKm = state.selectedRadiusKm,
                        page = 0,
                        append = false
                    )
                }
            }

            is SearchEvent.OnSearch -> {
                triggerSearch(state.query, state.selectedGenreId, state.selectedRadiusKm, page = 0, append = false)
            }

            is SearchEvent.OnGenreToggle -> {
                val newGenreId = if (event.genreId == state.selectedGenreId) null else event.genreId
                _uiState.value = state.copy(selectedGenreId = newGenreId)
                triggerSearch(state.query, newGenreId, state.selectedRadiusKm, page = 0, append = false)
            }

            is SearchEvent.OnRadiusChanged -> {
                _uiState.value = state.copy(selectedRadiusKm = event.radiusKm)
                triggerSearch(state.query, state.selectedGenreId, event.radiusKm, page = 0, append = false)
            }

            is SearchEvent.OnLoadMore -> {
                if (!state.hasMorePages || state.isLoadingMore) return
                triggerSearch(
                    query = state.query,
                    genreId = state.selectedGenreId,
                    radiusKm = state.selectedRadiusKm,
                    page = state.currentPage + 1,
                    append = true
                )
            }
        }
    }

    private fun triggerSearch(
        query: String,
        genreId: String?,
        radiusKm: Double,
        page: Int,
        append: Boolean
    ) {
        val location = currentLocation ?: run {
            Timber.w("No location available for search")
            return
        }

        val currentState = _uiState.value as? SearchScreenState.Success ?: return
        if (append) {
            _uiState.value = currentState.copy(isLoadingMore = true)
        }

        val expandedGenreIds = if (genreId != null) {
            GenreMatchHelper.expandGenresToEnumNames(setOf(genreId), cachedGenres)
        } else emptySet()

        getNearbyBooksUseCase(
            latitude = location.latitude,
            longitude = location.longitude,
            radiusInKm = radiusKm,
            query = query,
            expandedGenreIds = expandedGenreIds,
            page = page
        ).onEach { result ->
            val state = _uiState.value as? SearchScreenState.Success ?: return@onEach
            when (result) {
                is ApiResult.Loading -> {
                    if (!append) _uiState.value = state.copy(isSearching = true)
                }
                is ApiResult.Error -> {
                    _uiState.value = if (append) {
                        state.copy(isLoadingMore = false)
                    } else {
                        SearchScreenState.Error(result.message ?: Strings.Errors.UNKNOWN)
                    }
                }
                is ApiResult.Success -> {
                    val searchResult = result.data ?: return@onEach
                    val updatedListings = if (append) {
                        state.listings + searchResult.listings
                    } else {
                        searchResult.listings
                    }
                    val genresWithCounts = facetsToGenreWithCounts(searchResult.genreFacets)
                    _uiState.value = state.copy(
                        listings = updatedListings,
                        genresWithCounts = genresWithCounts,
                        hasMorePages = searchResult.hasMorePages,
                        currentPage = page,
                        isSearching = false,
                        isLoadingMore = false
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun fetchWithLocation() {
        viewModelScope.launch {
            locationFacade.getLastKnownLocation(
                applicationContext,
                onSuccess = { location ->
                    currentLocation = location
                    val state = _uiState.value
                    val genreId = (state as? SearchScreenState.Success)?.selectedGenreId
                    val radius = (state as? SearchScreenState.Success)?.selectedRadiusKm ?: RadiusOption.LARGE.km
                    _uiState.value = SearchScreenState.Loading
                    triggerSearch(query = "", genreId = genreId, radiusKm = radius, page = 0, append = false)
                },
                onFailure = { exception ->
                    _uiState.value = SearchScreenState.Error(
                        exception.message ?: Strings.Errors.LOCATION_FETCH_FAILED
                    )
                }
            )
        }
    }

    private fun facetsToGenreWithCounts(facets: Map<String, Int>): List<GenreWithCount> {
        val genreCounts = mutableMapOf<Genre, Int>()
        facets.forEach { (enumName, count) ->
            val matchingGenre = cachedGenres.find { genre ->
                GenreMatchHelper.getMatchingBookGenres(genre).any { it.name == enumName }
            }
            if (matchingGenre != null) {
                genreCounts[matchingGenre] = (genreCounts[matchingGenre] ?: 0) + count
            }
        }
        return genreCounts.entries
            .map { GenreWithCount(it.key, it.value) }
            .sortedByDescending { it.count }
    }

    fun checkPermission() {
        locationFacade.checkPermissionStatus(applicationContext)
    }

    fun getFacade(): LocationFacade = locationFacade

    private fun observePermissionChanges() {
        viewModelScope.launch {
            hasLocationPermission.collect { hasPermission ->
                if (hasPermission && currentLocation == null) {
                    Timber.d("Location permission granted — fetching location and searching")
                    fetchWithLocation()
                }
            }
        }
    }

}
