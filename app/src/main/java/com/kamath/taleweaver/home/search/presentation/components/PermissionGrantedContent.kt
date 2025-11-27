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
import com.kamath.taleweaver.core.components.TaleWeaverScaffold
import com.kamath.taleweaver.core.components.TopBars.AppBarType
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
    var isSearchActive by remember { mutableStateOf(false) }

    TaleWeaverScaffold(
        appBarType = AppBarType.Search(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = {
                isSearchActive = false
                Timber.d("Searching for: $searchQuery")
                // TODO: Trigger search via onEvent
            },
            active = isSearchActive,
            onActiveChange = { isSearchActive = it },
            placeholder = "Explore books nearby..."
        ),
        floatingActionButton = {
            if (state is SearchScreenState.Success) {
                RadiusSelector(
                    selectedRadius = state.radiusKm,
                    onRadiusSelected = { radiusKm ->
                        onEvent(SearchEvent.OnRadiusChanged(radiusKm))
                    },
                    modifier = Modifier.padding(bottom = 72.dp)  // Position above bottom nav bar
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Genre Filter Row
            if (state is SearchScreenState.Success && state.availableGenres.isNotEmpty()) {
                GenreFilterRow(
                    genres = state.availableGenres,
                    selectedGenreIds = state.selectedGenreIds,
                    onGenreToggle = { genreId -> onEvent(SearchEvent.OnGenreToggle(genreId)) }
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
                                contentPadding = PaddingValues(8.dp),
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