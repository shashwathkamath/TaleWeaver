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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.kamath.taleweaver.home.search.presentation.SearchEvent
import com.kamath.taleweaver.home.search.presentation.SearchScreenState
import com.kamath.taleweaver.ui.theme.BookAppBar
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PermissionGrantedContent(
    state: SearchScreenState,
    onEvent: (SearchEvent) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            BookAppBar(title = "Search Nearby Books")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Enter book title or genre") },
                singleLine = true
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
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
                                onClick = { Timber.d("Retry button clicked")},
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
                                    ListingGridItem(listing = listing)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}