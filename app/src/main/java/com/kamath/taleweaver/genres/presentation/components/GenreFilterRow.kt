package com.kamath.taleweaver.genres.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.genres.domain.model.Genre
import com.kamath.taleweaver.genres.domain.model.GenreWithCount
import com.kamath.taleweaver.genres.domain.util.GenrePopularityHelper
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

/**
 * Horizontal scrollable row of genre filter chips
 * Single selection genre filtering UI
 * Shows top 5 most popular genres + "More" chip
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreFilterRow(
    genres: List<Genre>,
    selectedGenreId: String?,
    onGenreToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    genresWithCounts: List<GenreWithCount> = emptyList()
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Determine which genres to show
    val (topGenres, hasMore) = if (genresWithCounts.isNotEmpty()) {
        val top = GenrePopularityHelper.getTopGenres(genresWithCounts, topN = 5)
        val remaining = GenrePopularityHelper.getRemainingGenres(genresWithCounts, topN = 5)
        Pair(top.map { it.genre }, remaining.isNotEmpty())
    } else {
        // Fallback to showing all genres if counts not available
        Pair(genres, false)
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(
            items = topGenres,
            key = { it.id }
        ) { genre ->
            GenreFilterChip(
                genre = genre,
                isSelected = genre.id == selectedGenreId,
                onToggle = { onGenreToggle(genre.id) }
            )
        }

        // Add "More" chip if there are additional genres
        if (hasMore) {
            item {
                MoreGenresChip(
                    onClick = {
                        scope.launch {
                            showBottomSheet = true
                        }
                    }
                )
            }
        }
    }

    // Bottom sheet for all genres
    if (showBottomSheet && genresWithCounts.isNotEmpty()) {
        GenreFilterBottomSheet(
            genresWithCounts = genresWithCounts,
            selectedGenreId = selectedGenreId,
            onGenreSelected = onGenreToggle,
            onDismiss = {
                scope.launch {
                    bottomSheetState.hide()
                    showBottomSheet = false
                }
            },
            sheetState = bottomSheetState
        )
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
 * "More" chip to open bottom sheet with all genres
 */
@Composable
private fun MoreGenresChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = {
            Text(
                text = "More",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "More genres"
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Parse hex color string to Compose Color
 */
private fun parseColor(hexColor: String): Color {
    return try {
        Color(hexColor.toColorInt())
    } catch (e: IllegalArgumentException) {
        Color.Gray
    }
}
