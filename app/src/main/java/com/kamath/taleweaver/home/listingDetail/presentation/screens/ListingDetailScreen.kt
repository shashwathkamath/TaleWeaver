package com.kamath.taleweaver.home.listingDetail.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamath.taleweaver.home.feed.domain.model.BookCondition
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.listingDetail.presentation.ListingDetailState
import com.kamath.taleweaver.home.listingDetail.presentation.ListingDetailViewModel
import com.kamath.taleweaver.core.components.TaleWeaverScaffold
import com.kamath.taleweaver.core.components.TopBars.AppBarType
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.home.listingDetail.presentation.components.ListingDetails

@Composable
fun ListingDetailScreen(
    viewModel: ListingDetailViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ListingDetailContent(
        uiState = uiState,
        onNavigateUp = onNavigateUp
    )
}


@Composable
private fun ListingDetailContent(
    uiState: ListingDetailState,
    onNavigateUp: () -> Unit
) {
    TaleWeaverScaffold(
        appBarType = AppBarType.Default(Strings.Titles.LISTING_DETAILS),
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = Strings.ContentDescriptions.BACK
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.error != null -> {
                    Text(
                        text = Strings.Formats.errorMessage(uiState.error),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                uiState.listing != null -> {
                    ListingDetails(listing = uiState.listing)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListingDetailContentLoadingPreview() {
    ListingDetailContent(
        uiState = ListingDetailState(isLoading = true),
        onNavigateUp = {}
    )
}

// Preview for the success state
@Preview(showBackground = true)
@Composable
fun ListingDetailContentSuccessPreview() {
    val dummyListing = Listing(
        title = "Project Hail Mary",
        author = "Andy Weir",
        price = 15.00,
        condition = BookCondition.LIKE_NEW,
        description = "A thrilling sci-fi novel about a lone astronaut on a mission to save humanity. Read once, in excellent condition.",
        sellerUsername = "SciFiSteve"
    )
    ListingDetailContent(
        uiState = ListingDetailState(listing = dummyListing),
        onNavigateUp = {}
    )
}