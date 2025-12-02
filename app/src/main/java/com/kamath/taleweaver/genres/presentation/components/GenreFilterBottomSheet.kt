package com.kamath.taleweaver.genres.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.core.components.TaleWeaverBottomSheet
import com.kamath.taleweaver.genres.domain.model.GenreWithCount

/**
 * Bottom sheet that displays all available genres sorted by popularity
 * Uses the common TaleWeaverBottomSheet for consistent theming
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreFilterBottomSheet(
    genresWithCounts: List<GenreWithCount>,
    selectedGenreId: String?,
    onGenreSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    TaleWeaverBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        title = "Select Genre",
        subtitle = "Choose a genre to filter books"
    ) {
        // Genre List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(genresWithCounts) { genreWithCount ->
                GenreFilterItem(
                    genreWithCount = genreWithCount,
                    isSelected = genreWithCount.id == selectedGenreId,
                    onSelected = {
                        onGenreSelected(genreWithCount.id)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun GenreFilterItem(
    genreWithCount: GenreWithCount,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Genre name and count
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = genreWithCount.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                if (genreWithCount.count > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${genreWithCount.count} books",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        }
                    )
                }
            }

            // Checkmark for selected genre
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
