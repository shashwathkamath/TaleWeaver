package com.kamath.taleweaver.home.account.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.core.components.TabChip
import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.home.account.presentation.AccountScreenViewModel
import com.kamath.taleweaver.home.account.presentation.AccountTab
import com.kamath.taleweaver.home.feed.domain.model.Listing

@Composable
fun AccountDetails(
    modifier: Modifier,
    viewModel: AccountScreenViewModel,
    userProfile: UserProfile,
    name: String,
    description: String,
    address: String,
    myListings: List<Listing>,
    isLoadingListings: Boolean,
    isUploadingPhoto: Boolean,
    selectedTab: AccountTab,
    purchases: List<com.kamath.taleweaver.order.domain.model.Order>,
    sales: List<com.kamath.taleweaver.order.domain.model.Order>,
    isLoadingPurchases: Boolean,
    isLoadingSales: Boolean,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onEditPhotoClick: () -> Unit,
    onTabSelected: (AccountTab) -> Unit,
    onListingClick: (String) -> Unit,
    onViewAllListingsClick: () -> Unit,
    onViewShippingLabelClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    onSubmitFeedback: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Fixed Profile Header
        ProfileHeader(
            userProfile = userProfile,
            onEditPhotoClick = onEditPhotoClick,
            isUploading = isUploadingPhoto
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Chips Row (Scrollable horizontally)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TabChip(
                    label = Strings.Labels.PROFILE_INFORMATION,
                    isSelected = selectedTab == AccountTab.PROFILE_INFO,
                    onClick = { onTabSelected(AccountTab.PROFILE_INFO) }
                )
            }
            item {
                TabChip(
                    label = Strings.Labels.MY_LISTINGS,
                    isSelected = selectedTab == AccountTab.MY_LISTINGS,
                    onClick = { onTabSelected(AccountTab.MY_LISTINGS) }
                )
            }
            item {
                TabChip(
                    label = Strings.Labels.SHIPMENTS,
                    isSelected = selectedTab == AccountTab.SHIPMENT,
                    onClick = { onTabSelected(AccountTab.SHIPMENT) }
                )
            }
            item {
                TabChip(
                    label = Strings.Labels.FEEDBACK,
                    isSelected = selectedTab == AccountTab.FEEDBACK,
                    onClick = { onTabSelected(AccountTab.FEEDBACK) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable Content based on selected tab
        // Note: SHIPMENT tab has its own LazyColumn, so we don't add verticalScroll for it
        when (selectedTab) {
            AccountTab.PROFILE_INFO -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileInfoContent(
                        name = name,
                        description = description,
                        address = address,
                        onNameChange = onNameChange,
                        onDescriptionChange = onDescriptionChange,
                        onAddressChange = onAddressChange
                    )
                }
            }
            AccountTab.MY_LISTINGS -> {
                // Refresh listings when this tab is displayed
                LaunchedEffect(Unit) {
                    viewModel.refreshUserListings()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MyListingsContent(
                        listings = myListings,
                        isLoading = isLoadingListings,
                        onListingClick = onListingClick,
                        onViewAllClick = onViewAllListingsClick
                    )
                }
            }
            AccountTab.SHIPMENT -> {
                // Refresh orders when this tab is displayed
                LaunchedEffect(Unit) {
                    viewModel.refreshUserOrders()
                }

                // No verticalScroll here - ShipmentTrackingContent has its own LazyColumn
                ShipmentTrackingContent(
                    purchases = purchases,
                    sales = sales,
                    isLoadingPurchases = isLoadingPurchases,
                    isLoadingSales = isLoadingSales,
                    onViewLabelClick = onViewShippingLabelClick
                )
            }
            AccountTab.FEEDBACK -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FeedbackContent(
                        onSubmitFeedback = onSubmitFeedback
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoContent(
    name: String,
    description: String,
    address: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddressChange: (String) -> Unit
) {
    EditableFields(
        name = name,
        description = description,
        address = address,
        onNameChange = onNameChange,
        onDescriptionChange = onDescriptionChange,
        onAddressChange = onAddressChange
    )
    Spacer(modifier = Modifier.height(100.dp))
}

@Composable
private fun MyListingsContent(
    listings: List<Listing>,
    isLoading: Boolean,
    onListingClick: (String) -> Unit,
    onViewAllClick: () -> Unit
) {
    MyListingsSection(
        listings = listings,
        isLoading = isLoading,
        onListingClick = onListingClick,
        onViewAllClick = onViewAllClick
    )
    Spacer(modifier = Modifier.height(100.dp))
}
