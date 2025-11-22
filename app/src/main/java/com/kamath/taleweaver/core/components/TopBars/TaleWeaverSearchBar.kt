package com.kamath.taleweaver.core.components.TopBars

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * TaleWeaver styled SearchBar wrapping Material3 SearchBar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaleWeaverSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = { onSearch() },
            active = active,
            onActiveChange = onActiveChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (active) 0.dp else 16.dp),
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            // Search suggestions can go here
        }
        if (!active) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
        }
    }
}