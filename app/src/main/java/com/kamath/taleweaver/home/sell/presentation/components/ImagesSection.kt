package com.kamath.taleweaver.home.sell.presentation.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ImagesSection(
    selectedImages: List<Uri>,
    coverImageFromApi: String?,
    imagesError: String?,
    onAddImages: () -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Photos *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (imagesError != null) {
                Text(
                    imagesError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add button
                item {
                    OutlinedCard(
                        onClick = onAddImages,
                        modifier = Modifier.size(100.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = "Add photo",
                                    modifier = Modifier.size(32.dp)
                                )
                                Text("Add", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // API cover image
                coverImageFromApi?.let { url ->
                    item {
                        Box(modifier = Modifier.size(100.dp)) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Cover from API",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // "From API" badge
                            Text(
                                "API",
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(bottomEnd = 8.dp)
                                    )
                                    .padding(4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Selected images
                items(selectedImages) { uri ->
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onRemoveImage(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.error,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}