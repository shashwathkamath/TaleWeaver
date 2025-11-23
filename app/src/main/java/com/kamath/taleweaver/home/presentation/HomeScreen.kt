package com.kamath.taleweaver.home.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kamath.taleweaver.core.navigation.AppDestination
import com.kamath.taleweaver.core.navigation.HomeTabs
import com.kamath.taleweaver.home.account.presentation.AccountScreen
import com.kamath.taleweaver.home.feed.presentation.FeedScreen
import com.kamath.taleweaver.home.listingDetail.presentation.screens.ListingDetailScreen
import com.kamath.taleweaver.home.search.presentation.SearchScreen
import com.kamath.taleweaver.home.sell.presentation.SellScreen
import timber.log.Timber

val tabs = listOf(
    HomeTabs.AllTales,
    HomeTabs.SearchBooks,
    HomeTabs.CreateTale,
    HomeTabs.Settings
)

@Composable
fun HomeScreen() {
    val tabNavController = rememberNavController()
    // Get system navigation bar height (for Samsung/Android devices with bottom nav buttons)
    val navigationBarPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // Add base padding + system navigation bar padding
    val bottomPadding = 24.dp + navigationBarPadding

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = bottomPadding)
                    .height(56.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .clip(RoundedCornerShape(28.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                contentColor = MaterialTheme.colorScheme.primary,
                windowInsets = WindowInsets(0, 0, 0, 0),
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                tabs.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = {
                            Text(
                                screen.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                maxLines = 1
                            )
                        },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        alwaysShowLabel = true,
                        onClick = {
                            tabNavController.navigate(screen.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = HomeTabs.AllTales.route,
        ) {
            navigation(
                startDestination = AppDestination.FEED_SCREEN,
                route = HomeTabs.AllTales.route
            ) {
                composable(route = AppDestination.FEED_SCREEN) {
                    FeedScreen(
                        onListingClick = { listingId ->
                            Timber.d("Listing clicked: $listingId")
                            tabNavController.navigate("${AppDestination.LISTING_DETAIL_SCREEN}/$listingId")
                        }
                    )
                }
                composable(
                    route = "${AppDestination.LISTING_DETAIL_SCREEN}/{${AppDestination.ARG_LISTING_ID}}",
                    arguments = listOf(navArgument(AppDestination.ARG_LISTING_ID) {
                        type = NavType.StringType
                    })
                ) {
                    ListingDetailScreen(
                        onNavigateUp = { tabNavController.navigateUp() }
                    )
                }
            }
            composable(HomeTabs.SearchBooks.route) { SearchScreen() }
            composable(HomeTabs.CreateTale.route) { SellScreen() }
            composable(HomeTabs.Settings.route) { AccountScreen(navController = tabNavController) }
        }
    }
}