package com.kamath.taleweaver.home.account.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kamath.taleweaver.core.components.MyBox
import com.kamath.taleweaver.core.components.TaleWeaverScaffold
import com.kamath.taleweaver.core.components.TopBars.AppBarType
import com.kamath.taleweaver.core.navigation.NavigationEvent
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.core.util.UiEvent
import com.kamath.taleweaver.home.account.presentation.components.AccountDetails
import timber.log.Timber
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavController,
    onListingClick: (String) -> Unit,
    onViewAllListingsClick: () -> Unit,
    viewModel: AccountScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val onEvent = viewModel::onEvent
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var showPhotoPickerSheet by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onEvent(AccountScreenEvent.OnProfilePhotoSelected(it)) }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempPhotoUri?.let { onEvent(AccountScreenEvent.OnProfilePhotoSelected(it)) }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File.createTempFile(
                "profile_photo_",
                ".jpg",
                context.cacheDir
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                    )
                }
            }
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToLogin -> {
                    Timber.d("Inside NavController")
                }

                else -> {}
            }
        }
    }

    TaleWeaverScaffold(
        appBarType = AppBarType.WithActions(
            title = Strings.Titles.ACCOUNT,
            actions = {
                val successState = uiState as? AccountScreenState.Success
                if (successState?.hasUnsavedChanges == true) {
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            onEvent(AccountScreenEvent.OnSaveClick)
                        },
                        enabled = !successState.isSaving
                    ) {
                        if (successState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = Strings.Buttons.SAVE,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (val state = uiState) {
            is AccountScreenState.Loading -> {
                MyBox(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    CircularProgressIndicator()
                }
            }

            is AccountScreenState.Success -> {
                if (state.userProfile != null) {
                    AccountDetails(
                        modifier = Modifier.padding(innerPadding),
                        userProfile = state.userProfile,
                        name = state.userProfile.username,
                        description = state.userProfile.description,
                        address = state.userProfile.address,
                        myListings = state.myListings,
                        isLoadingListings = state.isLoadingListings,
                        isUploadingPhoto = state.isUploadingPhoto,
                        onNameChange = { /* TODO */ },
                        onDescriptionChange = { newDesc ->
                            onEvent(AccountScreenEvent.OnDescriptionChange(newDesc))
                        },
                        onAddressChange = { newAddress ->
                            onEvent(AccountScreenEvent.OnAddressChange(newAddress))
                        },
                        onEditPhotoClick = { showPhotoPickerSheet = true },
                        onListingClick = onListingClick,
                        onViewAllListingsClick = onViewAllListingsClick,
                        onLogoutClick = { onEvent(AccountScreenEvent.OnLogoutClick) }
                    )
                } else {
                    MyBox(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        Text(Strings.Errors.PROFILE_LOAD_FAILED)
                    }
                }
            }

            is AccountScreenState.Error -> {
                MyBox(
                    modifier = Modifier.padding(innerPadding)
                ) {
                    Text(state.message)
                }
            }
        }
    }

    // Photo picker bottom sheet
    if (showPhotoPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoPickerSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = Strings.PhotoPicker.TITLE,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Take Photo option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPhotoPickerSheet = false
                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = Strings.PhotoPicker.TAKE_PHOTO,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = Strings.PhotoPicker.TAKE_PHOTO,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Choose from Gallery option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPhotoPickerSheet = false
                            galleryLauncher.launch("image/*")
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = Strings.PhotoPicker.CHOOSE_FROM_GALLERY,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = Strings.PhotoPicker.CHOOSE_FROM_GALLERY,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}