package com.kamath.taleweaver.home.account.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.search.presentation.components.ListingGridItem

@Composable
fun MyListingsSection(
    listings: List<Listing>,
    isLoading: Boolean,
    onListingClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
    sectionTitle: String = Strings.Labels.MY_LISTINGS
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header with title and View All button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (listings.isNotEmpty()) {
                TextButton(onClick = onViewAllClick) {
                    Text(
                        text = Strings.Buttons.VIEW_ALL,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            listings.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Strings.EmptyStates.NO_USER_LISTINGS,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listings.take(5), key = { it.id }) { listing ->
                        ListingGridItem(
                            listing = listing,
                            modifier = Modifier
                                .width(140.dp)
                                .height(200.dp),
                            onClick = { onListingClick(listing.id) },
                            showStatus = true
                        )
                    }

                    // Show "View More" card if there are more than 5 listings
                    if (listings.size > 5) {
                        item {
                            ViewMoreCard(
                                remainingCount = listings.size - 5,
                                onClick = onViewAllClick,
                                modifier = Modifier
                                    .width(140.dp)
                                    .height(200.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewMoreCard(
    remainingCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "+$remainingCount",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = Strings.Labels.MORE,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
