package com.kamath.taleweaver.home.account.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamath.taleweaver.core.components.TaleWeaverScaffold
import com.kamath.taleweaver.core.components.TopBars.AppBarType
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.core.util.UiEvent
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.search.presentation.components.ListingGridItem

@Composable
fun MyListingsScreen(
    onNavigateUp: () -> Unit,
    onListingClick: (String) -> Unit,
    onEditListing: (String) -> Unit,
    viewModel: MyListingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf<Listing?>(null) }
    var contextMenuListing by remember { mutableStateOf<Listing?>(null) }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    TaleWeaverScaffold(
        appBarType = AppBarType.WithBackButton(
            title = Strings.Labels.MY_LISTINGS,
            onBackClick = onNavigateUp
        ),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is MyListingsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is MyListingsUiState.Success -> {
                if (state.listings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Strings.EmptyStates.NO_USER_LISTINGS,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.listings, key = { it.id }) { listing ->
                            Box {
                                ListingGridItem(
                                    listing = listing,
                                    modifier = Modifier.height(220.dp),
                                    onClick = { contextMenuListing = listing },
                                    showStatus = true
                                )

                                // Context menu for edit/delete
                                DropdownMenu(
                                    expanded = contextMenuListing?.id == listing.id,
                                    onDismissRequest = { contextMenuListing = null },
                                    offset = DpOffset(8.dp, 0.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(Strings.Buttons.VIEW_DETAILS) },
                                        onClick = {
                                            contextMenuListing = null
                                            onListingClick(listing.id)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(Strings.Buttons.EDIT) },
                                        onClick = {
                                            contextMenuListing = null
                                            onEditListing(listing.id)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                Strings.Buttons.DELETE,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            contextMenuListing = null
                                            showDeleteDialog = listing
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is MyListingsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { listing ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(Strings.Dialogs.DELETE_LISTING_TITLE) },
            text = { Text(Strings.Dialogs.deleteListingMessage(listing.title ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteListing(listing.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text(
                        Strings.Buttons.DELETE,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(Strings.Buttons.CANCEL)
                }
            }
        )
    }
}
