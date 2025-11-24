package com.kamath.taleweaver.core.components.TopBars

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kamath.taleweaver.core.util.Strings


/**
 * Factory composable that renders the appropriate AppBar based on type
 */
@Composable
fun TaleWeaverAppBar(
    appBarType: AppBarType,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {}
) {
    when (appBarType) {
        is AppBarType.Default -> BookAppBar(
            title = appBarType.title,
            modifier = modifier,
            navigationIcon = navigationIcon
        )

        is AppBarType.Search -> TaleWeaverSearchBar(
            query = appBarType.query,
            onQueryChange = appBarType.onQueryChange,
            onSearch = appBarType.onSearch,
            active = appBarType.active,
            onActiveChange = appBarType.onActiveChange,
            placeholder = appBarType.placeholder,
            modifier = modifier
        )

        is AppBarType.WithActions -> BookAppBar(
            title = appBarType.title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = appBarType.actions
        )

        is AppBarType.WithBackButton -> BookAppBar(
            title = appBarType.title,
            modifier = modifier,
            navigationIcon = {
                IconButton(onClick = appBarType.onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = Strings.ContentDescriptions.BACK,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            actions = appBarType.actions ?: {}
        )
    }
}