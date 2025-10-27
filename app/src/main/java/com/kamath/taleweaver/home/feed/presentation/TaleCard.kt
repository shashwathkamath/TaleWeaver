package com.kamath.taleweaver.home.feed.presentation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.home.feed.domain.model.Tale
import java.util.Date

/**
 * A reusable composable that displays a summary of a single Tale in a Card format.
 *
 * @param tale The tale data to display.
 * @param onTaleClick A callback invoked when the card is clicked, returning the tale's ID.
 */
@Composable
fun TaleCard(
    tale: Tale,
    onTaleClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTaleClick(tale.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Author Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = tale.authorDisplayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${tale.authorUsername}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Tale Content ---
            Text(
                text = tale.title ?: "Untitled Tale",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tale.excerpt ?: tale.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Engagement Actions ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EngagementButton(
                    icon = Icons.Default.FavoriteBorder,
                    count = tale.likesCount,
                    description = "Likes"
                )
                EngagementButton(
                    icon = Icons.Default.Forum,
                    count = tale.subTalesCount,
                    description = "Sub-Tales"
                )
                EngagementButton(
                    icon = Icons.Default.Share,
                    count = tale.restacksCount,
                    description = "Restacks"
                )
            }
        }
    }
}

/**
 * A small private composable for displaying an engagement icon and its count.
 */
@Composable
private fun EngagementButton(icon: ImageVector, count: Long, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Formats large numbers for display (e.g., 1200 -> 1.2k).
 */
private fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000 -> "${(count / 100_000) / 10.0}M"
        count >= 1000 -> "${(count / 100) / 10.0}k"
        else -> count.toString()
    }
}


@Preview(showBackground = true)
@Composable
private fun TaleCardPreview() {
    MaterialTheme {
        val sampleTale = Tale(
            id = "sample123",
            isRootTale = true,
            authorId = "author123",
            authorUsername = "creative_writer",
            authorDisplayName = "Shashwath Kamath",
            title = "The Last Sunset on an Old World",
            content = "The sky bled orange and purple as the last sun of Old Earth dipped below the horizon. It wasn't a sad sight, but a promise that even in the deepest darkness, a new star could be born. And on the barren plains, something stirred.",
            excerpt = "The sky bled orange and purple as the last sun of Old Earth dipped below the horizon...",
            createdAt = Date(),
            likesCount = 1256,
            subTalesCount = 98,
            restacksCount = 234,
            shareCount = 56
        )
        TaleCard(tale = sampleTale, onTaleClick = {})
    }
}
