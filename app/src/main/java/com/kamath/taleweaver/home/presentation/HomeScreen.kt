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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kamath.taleweaver.core.navigation.HomeTabs
import com.kamath.taleweaver.home.feed.presentation.FeedScreen

val tabs = listOf<HomeTabs>(
    HomeTabs.AllTales,
    HomeTabs.MyTales,
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
            composable(HomeTabs.AllTales.route) { FeedScreen() }
            composable(HomeTabs.MyTales.route) { Text("My Touched Tales Screen - Coming Soon!") }
            composable(HomeTabs.CreateTale.route) { Text("Create Tale Screen - Coming Soon!") }
            composable(HomeTabs.Settings.route) { Text("Settings Screen - Coming Soon!") }
        }
    }
}