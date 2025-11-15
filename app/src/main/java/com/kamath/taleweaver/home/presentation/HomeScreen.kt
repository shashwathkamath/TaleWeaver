package com.kamath.taleweaver.home.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                tabs.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            tabNavController.navigate(screen.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = HomeTabs.AllTales.route,
            modifier = Modifier.padding(innerPadding)
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
            composable(HomeTabs.CreateTale.route) { Text("Create Tale Screen - Coming Soon!") }
            composable(HomeTabs.Settings.route) { AccountScreen(navController = tabNavController) }
        }
    }
}