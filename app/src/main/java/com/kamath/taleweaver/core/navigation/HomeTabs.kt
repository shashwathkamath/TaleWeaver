package com.kamath.taleweaver.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class HomeTabs(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object AllTales : HomeTabs(
        "all_listings",
        "Listings",
        Icons.Filled.Home
    )

    object SearchBooks : HomeTabs(
        "search",
        "search",
        Icons.Filled.Search
    )

    object CreateTale : HomeTabs(
        "create_listing",
        "Sell",
        Icons.Filled.Camera
    )

    object Settings : HomeTabs(
        "my_account",
        "Account",
        Icons.Filled.Settings
    )
}