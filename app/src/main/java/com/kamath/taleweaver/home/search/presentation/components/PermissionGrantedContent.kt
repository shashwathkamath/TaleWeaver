package com.kamath.taleweaver.home.search.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.core.components.FloatingSearchBar
import com.kamath.taleweaver.genres.presentation.components.GenreFilterRow
import com.kamath.taleweaver.home.search.presentation.SearchEvent
import com.kamath.taleweaver.home.search.presentation.SearchScreenState
import timber.log.Timber

@Composable
internal fun PermissionGrantedContent(
    state: SearchScreenState,
    onEvent: (SearchEvent) -> Unit,
    onListingClick: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Floating Search Bar at top
            FloatingSearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    onEvent(SearchEvent.OnQueryChanged(query))
                },
                onSearch = {
                    Timber.d("Searching for: $searchQuery")
                    onEvent(SearchEvent.OnQueryChanged(searchQuery))
                },
                placeholder = "Explore books nearby..."
            )
            // Genre Filter Row
            if (state is SearchScreenState.Success && state.availableGenres.isNotEmpty()) {
                GenreFilterRow(
                    genres = state.availableGenres,
                    selectedGenreId = state.selectedGenreId,
                    onGenreToggle = { genreId -> onEvent(SearchEvent.OnGenreToggle(genreId)) },
                    genresWithCounts = state.genresWithCounts
                )
            }

            // Content
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    is SearchScreenState.Loading -> {
                        //CircularProgressIndicator()
                    }

                    is SearchScreenState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.message,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Button(
                                onClick = { Timber.d("Retry button clicked") },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Retry")
                            }
                        }
                    }

                    is SearchScreenState.Success -> {
                        if (state.listings.isEmpty()) {
                            Text("No listings found nearby.")
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2), // 2 columns like Instagram
                                contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 100.dp),  // Extra padding for bottom nav
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.listings, key = { it.id }) { listing ->
                                    ListingGridItem(
                                        listing = listing,
                                        onClick = { onListingClick(listing.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}