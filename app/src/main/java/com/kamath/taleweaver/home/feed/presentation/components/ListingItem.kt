package com.kamath.taleweaver.home.feed.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kamath.taleweaver.core.util.Strings
import com.kamath.taleweaver.home.feed.domain.model.BookCondition
import com.kamath.taleweaver.home.feed.domain.model.BookGenre
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.feed.domain.model.ListingStatus

@SuppressLint("DefaultLocale")
@Composable
fun ListingItem(
    listing: Listing,
    onListingClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isOwnListing: Boolean = false
){
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onListingClick(listing.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(listing.primaryImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = Strings.ContentDescriptions.coverImage(listing.title),
                modifier = Modifier
                    .size(width = 80.dp, height = 120.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop,
//                // Add a placeholder in case the URL is null or loading
//                error = { /* You can show a placeholder drawable here */ },
//                placeholder = { /* You can show a placeholder drawable here */ }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = Strings.Formats.byAuthor(listing.author),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isOwnListing) Strings.Labels.YOU else Strings.Formats.sellerUsername(listing.sellerUsername),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isOwnListing) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isOwnListing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = String.format("$%.2f", listing.price),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = listing.condition.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListingItemPreview() {
    val dummyListing = Listing(
        id = "123",
        title = "The Name of the Wind",
        author = "Patrick Rothfuss",
        price = 12.50,
        condition = BookCondition.USED,
        genres = listOf(BookGenre.FANTASY),
        //coverImageUrls = listOf("https://example.com/image.jpg"),
        sellerUsername = "BookwormReader",
        status = ListingStatus.AVAILABLE,
    )
    ListingItem(listing = dummyListing, onListingClick = {})
}