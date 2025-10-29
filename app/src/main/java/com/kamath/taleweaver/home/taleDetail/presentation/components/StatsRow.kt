package com.kamath.taleweaver.home.taleDetail.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.home.feed.domain.model.Tale

@Composable
fun StatsRow(tale: Tale) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(
            icon = Icons.Default.FavoriteBorder,
            value = tale.likesCount.toString(),
            description = "Likes"
        )
        StatItem(
            icon = Icons.Default.Visibility,
            value = tale.readCount.toString(),
            description = "Reads"
        )
    }
}
