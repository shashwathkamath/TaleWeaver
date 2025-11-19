package com.kamath.taleweaver.home.feed.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamath.taleweaver.home.feed.presentation.components.ListingItem
import com.kamath.taleweaver.ui.theme.TaleWeaverScaffold
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
internal fun FeedScreen(
    viewmodel: FeedViewModel = hiltViewModel(),
    onListingClick: (String) -> Unit,
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(message = error)
        }
    }
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .distinctUntilChanged()
            .collect { layoutInfo ->
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                val totalItemsCount = layoutInfo.totalItemsCount
                val threshold = 3

                if (totalItemsCount > 0 && lastVisibleItemIndex >= totalItemsCount - 1 - threshold) {
                    if (!uiState.isLoadingMore && !uiState.endReached) {
                        viewmodel.onEvent(FeedEvent.LoadMore)
                    }
                }
            }
    }

    FeedScreenContent(
        uiState = uiState,
        lazyListState = lazyListState,
        snackbarHostState = snackbarHostState,
        onListingClick = { listingId -> onListingClick(listingId) },
        onSeedDatabase = { viewmodel.onEvent(FeedEvent.SeedDatabase) } // Pass the event handler
    )
}

@Composable
internal fun FeedScreenContent(
    uiState: FeedScreenState,
    lazyListState: LazyListState,
    snackbarHostState: SnackbarHostState,
    onListingClick: (String) -> Unit,
    onSeedDatabase: () -> Unit
) {
    TaleWeaverScaffold(
        title = "Feed",
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            IconButton(onClick = onSeedDatabase) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Seed Database"
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
            if (uiState.isLoading && uiState.listings.isEmpty()) {
                CircularProgressIndicator()
            } else if (!uiState.isLoading && uiState.listings.isEmpty()) {
                Text(
                    text = "No listings found.\nTap the '+' to seed the database.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.listings,
                        key = { listing -> listing.id }
                    ) { listing ->
                        ListingItem(
                            listing = listing,
                            onListingClick = { onListingClick(listing.id) }
                        )
                    }
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}
