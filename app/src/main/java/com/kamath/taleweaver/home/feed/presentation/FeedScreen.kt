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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamath.taleweaver.home.feed.presentation.components.TaleCard
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

@Composable
internal fun FeedScreen(
    viewmodel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewmodel.uiState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(message = error)
        }
    }
    // This LaunchedEffect is correct.
    // It triggers when the user scrolls to the bottom.
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo }
            .distinctUntilChanged()
            .collect { layoutInfo ->
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                val totalItemsCount = layoutInfo.totalItemsCount
                val threshold = 3 // buffer to start loading more before the absolute end

                // Check if we're near the end of the list and need to load more
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
        onTaleClick = { taleId -> /* TODO: Navigate to TaleDetailScreen(taleId) */ },
        onSeedDatabase = { viewmodel.onEvent(FeedEvent.SeedDatabase) } // Pass the event handler
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedScreenContent(
    uiState: FeedScreenState,
    lazyListState: LazyListState,
    snackbarHostState: SnackbarHostState,
    onTaleClick: (String) -> Unit,
    onSeedDatabase: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Feed")},
                actions = {
                    IconButton(onClick = onSeedDatabase) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Seed Database"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // --- THIS IS THE KEY CHANGE ---
            // Only show the main loader if the list is empty and we are loading for the first time.
            if (uiState.isLoading && uiState.tales.isEmpty()) {
                CircularProgressIndicator()
            } else if (!uiState.isLoading && uiState.tales.isEmpty()) {
                // This branch is for when loading is finished and there are truly no tales.
                Text(
                    text = "No tales found.\nTap the '+' to seed the database.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            } else {
                // This `else` block ensures the LazyColumn is *always* in the composition
                // as long as there are tales to show, even during a refresh.
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.tales,
                        key = { tale -> tale.id } // Use the unique tale ID for better performance
                    ) { tale ->
                        TaleCard(
                            tale = tale,
                            onTaleClick = onTaleClick
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
