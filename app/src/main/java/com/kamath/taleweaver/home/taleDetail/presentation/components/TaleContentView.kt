package com.kamath.taleweaver.home.taleDetail.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kamath.taleweaver.home.feed.domain.model.Tale
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TaleContentView(tale: Tale) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        AuthorInfo(tale)
        Spacer(modifier = Modifier.height(24.dp))

        tale.title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = tale.createdAt?.let {
                SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(it)
            } ?: "",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = tale.content,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 26.sp // Increased line height for better readability
        )
        Spacer(modifier = Modifier.height(32.dp))

        HorizontalDivider(
            Modifier,
            DividerDefaults.Thickness,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        StatsRow(tale)
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Sub-Tales",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Other users will be able to add their own branches to this story here. Stay tuned!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}