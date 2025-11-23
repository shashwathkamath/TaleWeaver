package com.kamath.taleweaver.home.sell.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.UiEvent
import com.kamath.taleweaver.home.account.domain.usecase.GetUserProfileUseCase
import com.kamath.taleweaver.home.feed.domain.model.BookCondition
import com.kamath.taleweaver.home.feed.domain.model.BookGenre
import com.kamath.taleweaver.home.sell.domain.model.CreateListingRequest
import com.kamath.taleweaver.home.sell.domain.usecases.CreateListingWithImagesUseCase
import com.kamath.taleweaver.home.sell.domain.usecases.FetchBookByIsbnUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SellScreenState(
    // ISBN & Book Fetch
    val isbn: String = "",
    val isbnError: String? = null,
    val isFetchingBook: Boolean = false,
    val bookFetched: Boolean = false,  // True after successful fetch

    // Book details (auto-filled from API)
    val title: String = "",
    val author: String = "",  // Combined from authors list
    val description: String = "",
    val selectedGenres: List<BookGenre> = emptyList(),
    val coverImageFromApi: String? = null,  // Pre-filled cover from API
    val originalPrice: Double? = null,  // Original retail price from Google Books
    val originalPriceCurrency: String? = null,

    // Manual input fields
    val price: String = "",
    val condition: BookCondition? = null,
    val shippingOffered: Boolean = false,
    val selectedImageUris: List<Uri> = emptyList(),

    // UI State
    val isLoading: Boolean = false,
    val showScanner: Boolean = false,
    val hasUserLocation: Boolean = true,
    val isCheckingLocation: Boolean = true,

    // Validation errors
    val titleError: String? = null,
    val authorError: String? = null,
    val priceError: String? = null,
    val conditionError: String? = null,
    val imagesError: String? = null
)

sealed interface SellScreenEvent {
    // ISBN & Scanning
    data class OnIsbnChange(val isbn: String) : SellScreenEvent
    data class OnIsbnScanned(val isbn: String) : SellScreenEvent
    object OnScanClick : SellScreenEvent
    object OnDismissScanner : SellScreenEvent
    object OnFetchBookDetails : SellScreenEvent
    object OnClearBookDetails : SellScreenEvent

    // Book details (editable after fetch)
    data class OnTitleChange(val title: String) : SellScreenEvent
    data class OnAuthorChange(val author: String) : SellScreenEvent
    data class OnDescriptionChange(val description: String) : SellScreenEvent
    data class OnGenreToggle(val genre: BookGenre) : SellScreenEvent

    // Manual input
    data class OnPriceChange(val price: String) : SellScreenEvent
    data class OnConditionSelect(val condition: BookCondition) : SellScreenEvent
    data class OnShippingToggle(val offered: Boolean) : SellScreenEvent

    // Images
    data class OnImagesSelected(val uris: List<Uri>) : SellScreenEvent
    data class OnImageRemove(val uri: Uri) : SellScreenEvent

    // Actions
    object OnSubmit : SellScreenEvent
    object OnClearForm : SellScreenEvent
}

@HiltViewModel
class SellScreenViewModel @Inject constructor(
    private val fetchBookByIsbnUseCase: FetchBookByIsbnUseCase,
    private val createListingWithImagesUseCase: CreateListingWithImagesUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SellScreenState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        checkUserLocation()
    }

    private fun checkUserLocation() {
        getUserProfileUseCase().onEach { result ->
            when (result) {
                is ApiResult.Loading -> {
                    _uiState.update { it.copy(isCheckingLocation = true) }
                }
                is ApiResult.Success -> {
                    val hasAddress = !result.data?.address.isNullOrBlank()
                    _uiState.update {
                        it.copy(
                            hasUserLocation = hasAddress,
                            isCheckingLocation = false
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            hasUserLocation = false,
                            isCheckingLocation = false
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: SellScreenEvent) {
        when (event) {
            is SellScreenEvent.OnIsbnChange -> {
                _uiState.update { it.copy(isbn = event.isbn, isbnError = null) }
            }

            is SellScreenEvent.OnIsbnScanned -> {
                _uiState.update {
                    it.copy(
                        isbn = event.isbn,
                        isbnError = null,
                        showScanner = false
                    )
                }
                fetchBookDetails(event.isbn)
            }

            is SellScreenEvent.OnScanClick -> {
                _uiState.update { it.copy(showScanner = true) }
            }

            is SellScreenEvent.OnDismissScanner -> {
                _uiState.update { it.copy(showScanner = false) }
            }

            is SellScreenEvent.OnFetchBookDetails -> {
                val isbn = _uiState.value.isbn
                if (isbn.isNotBlank()) {
                    fetchBookDetails(isbn)
                } else {
                    _uiState.update { it.copy(isbnError = "Enter ISBN first") }
                }
            }

            is SellScreenEvent.OnClearBookDetails -> {
                _uiState.update {
                    it.copy(
                        bookFetched = false,
                        title = "",
                        author = "",
                        description = "",
                        selectedGenres = emptyList(),
                        coverImageFromApi = null
                    )
                }
            }

            is SellScreenEvent.OnTitleChange -> {
                _uiState.update { it.copy(title = event.title, titleError = null) }
            }

            is SellScreenEvent.OnAuthorChange -> {
                _uiState.update { it.copy(author = event.author, authorError = null) }
            }

            is SellScreenEvent.OnDescriptionChange -> {
                _uiState.update { it.copy(description = event.description) }
            }

            is SellScreenEvent.OnGenreToggle -> {
                val current = _uiState.value.selectedGenres
                val updated = if (event.genre in current) {
                    current - event.genre
                } else {
                    current + event.genre
                }
                _uiState.update { it.copy(selectedGenres = updated) }
            }

            // Manual input
            is SellScreenEvent.OnPriceChange -> {
                _uiState.update { it.copy(price = event.price, priceError = null) }
            }

            is SellScreenEvent.OnConditionSelect -> {
                _uiState.update { it.copy(condition = event.condition, conditionError = null) }
            }

            is SellScreenEvent.OnShippingToggle -> {
                _uiState.update { it.copy(shippingOffered = event.offered) }
            }

            is SellScreenEvent.OnImagesSelected -> {
                val current = _uiState.value.selectedImageUris
                _uiState.update {
                    it.copy(
                        selectedImageUris = current + event.uris,
                        imagesError = null
                    )
                }
            }

            is SellScreenEvent.OnImageRemove -> {
                val updated = _uiState.value.selectedImageUris - event.uri
                _uiState.update { it.copy(selectedImageUris = updated) }
            }

            // Actions
            is SellScreenEvent.OnSubmit -> submitListing()
            is SellScreenEvent.OnClearForm -> {
                _uiState.value = SellScreenState()
            }
        }
    }

    private fun fetchBookDetails(isbn: String) {
        viewModelScope.launch {
            fetchBookByIsbnUseCase(isbn).collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        _uiState.update { it.copy(isFetchingBook = true) }
                    }

                    is ApiResult.Success -> {
                        val book = result.data
                        _uiState.update {
                            it.copy(
                                isFetchingBook = false,
                                bookFetched = true,
                                title = book?.title ?: "",
                                author = book?.authors?.joinToString(", ") ?: "",
                                description = book?.description ?: "",
                                selectedGenres = mapGenres(book?.genres),
                                coverImageFromApi = book?.coverImageUrl,
                                originalPrice = book?.originalPrice,
                                originalPriceCurrency = book?.originalPriceCurrency
                            )
                        }
                        _eventFlow.emit(UiEvent.ShowSnackbar("Book details loaded!"))
                    }

                    is ApiResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isFetchingBook = false,
                                isbnError = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun mapGenres(apiGenres: List<String>?): List<BookGenre> {
        if (apiGenres.isNullOrEmpty()) return emptyList()

        return apiGenres.mapNotNull { genre ->
            val normalized = genre.uppercase().replace(" ", "_")
            BookGenre.entries.find {
                it.name == normalized || genre.contains(it.name, ignoreCase = true)
            }
        }.distinct()
    }

    private fun submitListing() {
        if (!validateForm()) return

        val state = _uiState.value

        val request = CreateListingRequest(
            title = state.title.trim(),
            author = state.author.trim(),
            isbn = state.isbn.trim(),
            description = state.description.trim(),
            genres = state.selectedGenres,
            price = state.price.toDoubleOrNull() ?: 0.0,
            originalPrice = state.originalPrice,
            originalPriceCurrency = state.originalPriceCurrency,
            condition = state.condition!!,
            shippingOffered = state.shippingOffered
        )

        viewModelScope.launch {
            createListingWithImagesUseCase(request, state.selectedImageUris)
                .collect { result ->
                    when (result) {
                        is ApiResult.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }

                        is ApiResult.Success -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _eventFlow.emit(UiEvent.ShowSnackbar("Listing created successfully!"))
                            _uiState.value = SellScreenState()
                        }

                        is ApiResult.Error -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "Failed"))
                        }
                    }
                }
        }
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title is required") }
            isValid = false
        }
        if (state.author.isBlank()) {
            _uiState.update { it.copy(authorError = "Author is required") }
            isValid = false
        }
        if (state.price.isBlank() || state.price.toDoubleOrNull() == null) {
            _uiState.update { it.copy(priceError = "Valid price is required") }
            isValid = false
        }
        if (state.condition == null) {
            _uiState.update { it.copy(conditionError = "Select condition") }
            isValid = false
        }
        if (state.selectedImageUris.isEmpty() && state.coverImageFromApi == null) {
            _uiState.update { it.copy(imagesError = "Add at least one image") }
            isValid = false
        }

        return isValid
    }
}