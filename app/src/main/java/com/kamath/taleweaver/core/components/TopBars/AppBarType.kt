package com.kamath.taleweaver.core.components.TopBars

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

/**
 * Sealed class representing different types of AppBar configurations
 */
sealed class AppBarType {
    data class Default(val title: String) : AppBarType()

    class Search(
        val query: String,
        val onQueryChange: (String) -> Unit,
        val onSearch: () -> Unit = {},
        val active: Boolean = false,
        val onActiveChange: (Boolean) -> Unit = {},
        val placeholder: String = "Search..."
    ) : AppBarType()

    class WithActions(
        val title: String,
        val actions: @Composable RowScope.() -> Unit
    ) : AppBarType()

    class WithBackButton(
        val title: String,
        val onBackClick: () -> Unit,
        val actions: @Composable (RowScope.() -> Unit)? = null
    ) : AppBarType()
}