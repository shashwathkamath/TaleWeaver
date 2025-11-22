package com.kamath.taleweaver.core.components.TopBars

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


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
    }
}