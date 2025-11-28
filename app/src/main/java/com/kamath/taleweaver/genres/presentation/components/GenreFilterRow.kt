package com.kamath.taleweaver.genres.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.genres.domain.model.Genre

/**
 * Horizontal scrollable row of genre filter chips
 * Single selection genre filtering UI
 */
@Composable
fun GenreFilterRow(
    genres: List<Genre>,
    selectedGenreId: String?,  // Changed from Set<String> to nullable String for single selection
    onGenreToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(
            items = genres,
            key = { it.id }
        ) { genre ->
            GenreFilterChip(
                genre = genre,
                isSelected = genre.id == selectedGenreId,  // Single selection check
                onToggle = { onGenreToggle(genre.id) }
            )
        }
    }
}

@Composable
private fun GenreFilterChip(
    genre: Genre,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipColor = parseColor(genre.color)

    FilterChip(
        selected = isSelected,
        onClick = onToggle,
        label = {
            Text(
                text = genre.displayName,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = chipColor.copy(alpha = 0.3f),
            selectedLabelColor = chipColor,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = if (isSelected) {
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = true,
                borderColor = chipColor,
                selectedBorderColor = chipColor
            )
        } else {
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = false
            )
        }
    )
}

/**
 * Parse hex color string to Compose Color
 */
private fun parseColor(hexColor: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hexColor))
    } catch (e: IllegalArgumentException) {
        Color.Gray
    }
}
