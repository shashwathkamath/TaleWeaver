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
import androidx.compose.material3.MaterialTheme
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
import com.kamath.taleweaver.core.components.ButtonVariant
import com.kamath.taleweaver.core.components.FloatingSearchBar
import com.kamath.taleweaver.core.components.TaleWeaverButton
import com.kamath.taleweaver.core.components.TaleWeaverScaffold
import com.kamath.taleweaver.core.components.TopBars.AppBarType
import com.kamath.taleweaver.core.util.Strings
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

    TaleWeaverScaffold(appBarType = AppBarType.Default(Strings.Titles.SEARCH)) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FloatingSearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    onEvent(SearchEvent.OnQueryChanged(query))
                },
                onSearch = {
                    onEvent(SearchEvent.OnQueryChanged(searchQuery))
                },
                placeholder = Strings.Placeholders.SEARCH_NEARBY
            )

            if (state is SearchScreenState.Success && state.availableGenres.isNotEmpty()) {
                GenreFilterRow(
                    genres = state.availableGenres,
                    selectedGenreId = state.selectedGenreId,
                    onGenreToggle = { genreId -> onEvent(SearchEvent.OnGenreToggle(genreId)) },
                    genresWithCounts = state.genresWithCounts
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (state) {
                    is SearchScreenState.Loading -> Unit

                    is SearchScreenState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 32.dp)
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            TaleWeaverButton(
                                text = Strings.Buttons.RETRY,
                                onClick = { Timber.d("Retry button clicked") },
                                variant = ButtonVariant.Primary
                            )
                        }
                    }

                    is SearchScreenState.Success -> {
                        if (state.listings.isEmpty()) {
                            Text(
                                text = Strings.EmptyStates.NO_NEARBY_LISTINGS,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 32.dp)
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(8.dp, 8.dp, 8.dp, 100.dp),
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

                        RadiusSelector(
                            selectedRadius = state.selectedRadiusKm,
                            onRadiusSelected = { onEvent(SearchEvent.OnRadiusChanged(it)) },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 88.dp)
                        )
                    }
                }
            }
        }
    }
}
