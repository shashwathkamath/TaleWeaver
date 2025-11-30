package com.kamath.taleweaver.home.account.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamath.taleweaver.core.components.BookPageLoadingAnimation
import com.kamath.taleweaver.core.components.TaleWeaverScaffold
import com.kamath.taleweaver.core.components.TopBars.AppBarType
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.home.account.presentation.components.AccountDetails

@Composable
fun UserProfileScreen(
    userId: String,
    onNavigateUp: () -> Unit,
    onListingClick: (String) -> Unit,
    onViewAllListingsClick: () -> Unit = {},
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load user profile when screen is displayed
    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    // Create a dummy AccountScreenViewModel instance (won't be used for state, just for component compatibility)
    val dummyViewModel = hiltViewModel<AccountScreenViewModel>()

    TaleWeaverScaffold(
        appBarType = AppBarType.Default("User Profile"),
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = Strings.ContentDescriptions.BACK
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is UserProfileState.Loading -> {
                    BookPageLoadingAnimation(
                        size = 48.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is UserProfileState.Success -> {
                    if (state.userProfile != null) {
                        AccountDetails(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = dummyViewModel, // Just for component compatibility, not used for state
                            userProfile = state.userProfile,
                            name = state.userProfile.username,
                            description = state.userProfile.description,
                            address = state.userProfile.address,
                            myListings = state.listings,
                            isLoadingListings = state.isLoadingListings,
                            isUploadingPhoto = false,
                            selectedTab = state.selectedTab,
                            purchases = emptyList(),
                            sales = emptyList(),
                            isLoadingPurchases = false,
                            isLoadingSales = false,
                            isCurrentUser = false, // KEY: This is another user's profile
                            onNameChange = { /* Read-only */ },
                            onDescriptionChange = { /* Read-only */ },
                            onAddressChange = { /* Read-only */ },
                            onEditPhotoClick = { /* Disabled */ },
                            onTabSelected = { tab -> viewModel.onTabSelected(tab) },
                            onListingClick = onListingClick,
                            onViewAllListingsClick = onViewAllListingsClick,
                            onViewShippingLabelClick = { /* Not applicable */ },
                            onLogoutClick = { /* Not applicable */ },
                            onSubmitFeedback = { /* Not applicable */ }
                        )
                    } else {
                        Text(
                            text = "User not found",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                is UserProfileState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
