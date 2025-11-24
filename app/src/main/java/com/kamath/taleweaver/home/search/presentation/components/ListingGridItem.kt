package com.kamath.taleweaver.home.search.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.feed.domain.model.ListingStatus

@Composable
fun ListingGridItem(
    listing: Listing,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showStatus: Boolean = false
) {
    Card(
        modifier = modifier
            .aspectRatio(0.75f)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(listing.primaryImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = Strings.ContentDescriptions.coverFor(listing.title ?: Strings.Labels.NO_TITLE),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Status badge overlay
                if (showStatus && listing.status != ListingStatus.AVAILABLE) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                    StatusBadge(
                        status = listing.status,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Status indicator in corner for available items
                if (showStatus && listing.status == ListingStatus.AVAILABLE) {
                    StatusBadge(
                        status = listing.status,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = listing.title ?: Strings.Labels.NO_TITLE,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = Strings.Formats.price(listing.price),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                listing.distanceKm?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    val miles = it * 0.621371
                    Text(
                        text = Strings.Formats.milesAway(miles),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: ListingStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        ListingStatus.AVAILABLE -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        ListingStatus.SOLD -> Pair(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError
        )
        ListingStatus.RESERVED -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
    }

    Text(
        text = status.status,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = textColor
    )
}

@Preview(showBackground = true)
@Composable
private fun ListingGridItemPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            ListingGridItem(
                listing = Listing(
                    id = "preview_1",
                    title = "The Midnight Library",
                    sellerUsername = "shashwath",
                    price = 12.99,
                    status = ListingStatus.AVAILABLE,
                    distanceKm = 8.2
                ),
                modifier = Modifier.width(150.dp),
                onClick = {},
                showStatus = true
            )
        }
    }
}
