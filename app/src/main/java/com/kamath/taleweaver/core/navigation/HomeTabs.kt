package com.kamath.taleweaver.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class HomeTabs(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object AllTales : HomeTabs(
        "all_tales",
        "All Tales",
        Icons.Filled.Home
    )

    object MyTales : HomeTabs(
        "my_tales",
        "My Tales",
        Icons.Filled.List
    )

    object CreateTale : HomeTabs(
        "create_tale",
        "Create Tale",
        Icons.Filled.Add
    )

    object Settings : HomeTabs(
        "settings",
        "Settings",
        Icons.Filled.Settings
    )
}