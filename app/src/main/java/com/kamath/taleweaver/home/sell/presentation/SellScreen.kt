package com.kamath.taleweaver.home.sell.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamath.taleweaver.core.components.BookPageLoadingAnimation
import com.kamath.taleweaver.core.components.ButtonVariant
import com.kamath.taleweaver.core.components.TaleWeaverButton
import com.kamath.taleweaver.core.components.TaleWeaverScaffold
import com.kamath.taleweaver.core.components.TopBars.AppBarType
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.core.util.UiEvent
import com.kamath.taleweaver.home.sell.presentation.components.BookDetailsSection
import com.kamath.taleweaver.home.sell.presentation.components.ImagesSection
import com.kamath.taleweaver.home.sell.presentation.components.IsbnScannerScreen
import com.kamath.taleweaver.home.sell.presentation.components.IsbnSection
import com.kamath.taleweaver.home.sell.presentation.components.ListingDetailsSection
import com.kamath.taleweaver.home.sell.presentation.components.PhotoCaptureScreen

@Composable
fun SellScreen(
    viewModel: SellScreenViewModel = hiltViewModel(),
    onCameraStateChanged: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val onEvent = viewModel::onEvent
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // Refresh location check when screen becomes visible
    LifecycleResumeEffect(Unit) {
        onEvent(SellScreenEvent.OnScreenVisible)
        onPauseOrDispose { }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.ClearFocus -> {
                    focusManager.clearFocus()
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onEvent(SellScreenEvent.OnImagesSelected(uris))
        }
    }

    // Notify parent when camera state changes
    LaunchedEffect(uiState.currentPhotoStep) {
        onCameraStateChanged(uiState.currentPhotoStep != null)
    }

    TaleWeaverScaffold(
        appBarType = AppBarType.Default(Strings.Titles.SELL),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isCheckingLocation -> {
                // Loading state while checking location
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    BookPageLoadingAnimation(
                        size = 20.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            !uiState.hasUserLocation -> {
                // Show message to set location in account screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = Strings.Permissions.LOCATION_REQUIRED,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = Strings.Permissions.LOCATION_HELP,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            uiState.showScanner -> {
                // ISBN Scanner Overlay
                IsbnScannerScreen(
                    onIsbnScanned = { isbn -> onEvent(SellScreenEvent.OnIsbnScanned(isbn)) },
                    onDismiss = { onEvent(SellScreenEvent.OnDismissScanner) }
                )
            }

            uiState.currentPhotoStep != null -> {
                // Photo Capture Screen with prompt
                val currentStep = uiState.currentPhotoStep
                if (currentStep != null) {
                    PhotoCaptureScreen(
                        photoStep = currentStep,
                        onPhotoCaptured = { uri ->
                            onEvent(SellScreenEvent.OnPreparePhotoUri(uri))
                            onEvent(SellScreenEvent.OnPhotoCaptured(true))
                        },
                        onDismiss = { onEvent(SellScreenEvent.OnPhotoCaptured(false)) }
                    )
                }
            }

            else -> {
                Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IsbnSection(
                    isbn = uiState.isbn,
                    isbnError = uiState.isbnError,
                    isFetching = uiState.isFetchingBook,
                    onIsbnChange = { onEvent(SellScreenEvent.OnIsbnChange(it)) },
                    onScanClick = { onEvent(SellScreenEvent.OnScanClick) },
                    onFetchClick = { onEvent(SellScreenEvent.OnFetchBookDetails) }
                )
                if (uiState.bookFetched || uiState.title.isNotBlank()) {
                    BookDetailsSection(
                        title = uiState.title,
                        author = uiState.author,
                        description = uiState.description,
                        titleError = uiState.titleError,
                        authorError = uiState.authorError,
                        onTitleChange = { onEvent(SellScreenEvent.OnTitleChange(it)) },
                        onAuthorChange = { onEvent(SellScreenEvent.OnAuthorChange(it)) },
                        onDescriptionChange = { onEvent(SellScreenEvent.OnDescriptionChange(it)) }
                    )
                }
                ListingDetailsSection(
                    price = uiState.price,
                    condition = uiState.condition,
                    shippingOffered = uiState.shippingOffered,
                    sellerNotes = uiState.sellerNotes,
                    priceError = uiState.priceError,
                    conditionError = uiState.conditionError,
                    onPriceChange = { onEvent(SellScreenEvent.OnPriceChange(it)) },
                    onConditionSelect = { onEvent(SellScreenEvent.OnConditionSelect(it)) },
                    onShippingToggle = { onEvent(SellScreenEvent.OnShippingToggle(it)) },
                    onSellerNotesChange = { onEvent(SellScreenEvent.OnSellerNotesChange(it)) }
                )
                ImagesSection(
                    selectedImages = uiState.selectedImageUris,
                    coverImageFromApi = uiState.coverImageFromApi,
                    imagesError = uiState.imagesError,
                    currentPhotoStep = uiState.currentPhotoStep,
                    onStartCapture = { onEvent(SellScreenEvent.OnStartPhotoCapture) },
                    onRemoveImage = { onEvent(SellScreenEvent.OnImageRemove(it)) }
                )

                TaleWeaverButton(
                    onClick = { onEvent(SellScreenEvent.OnSubmit) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.Primary
                ) {
                    if (uiState.isLoading) {
                        BookPageLoadingAnimation(
                            size = 20.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            Strings.Buttons.CREATE_LISTING,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}}