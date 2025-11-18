package com.kamath.taleweaver.home.search.presentation

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.Constants.RADIUS_IN_KM
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
}

sealed interface SearchScreenState {
    object Loading : SearchScreenState
    data class Success(
        val listings: List<Listing> = emptyList(),
        val query: String = ""
    ) : SearchScreenState

    data class Error(val message: String) : SearchScreenState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val locationFacade: LocationFacade,
    private val searchNearbyBooksUseCase: SearchNearByBooksUseCase,
    private val getNearbyBooksUseCase: GetNearByBooksUseCase,
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

    init {
        checkPermission()
        migrateExistingListings()
        getNearbyBooks()
    }


    private fun getNearbyBooks() {
        _uiState.value = SearchScreenState.Loading
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            locationFacade.getLastKnownLocation(
                applicationContext,
                onSuccess = { location ->
                    fetchAllBooksAtLocation(location)
                },
                onFailure = { exception ->
                    _uiState.value = SearchScreenState.Error(
                        exception.message ?: "Could not retrieve device location"
                    )
                }
            )
        }
    }

    private fun fetchAllBooksAtLocation(location: Location) {
        getNearbyBooksUseCase(
            location.latitude,
            location.longitude,
            radiusInKm = RADIUS_IN_KM
        ).onEach { result ->
            val newState = when (result) {
                is ApiResult.Success -> {
                    SearchScreenState.Success(
                        listings = result.data ?: emptyList(),
                        query = ""
                    )
                }

                is ApiResult.Error -> {
                    SearchScreenState.Error(result.message ?: "An unknown Error occurred")
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
                    "Migration failed: ${error.message}"
                )
            }
        }
    }
}