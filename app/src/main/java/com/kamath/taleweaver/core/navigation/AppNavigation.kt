package com.kamath.taleweaver.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import authNavGraph
import com.kamath.taleweaver.home.presentation.HomeScreen
import com.kamath.taleweaver.splash.presentation.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppDestination.SPLASH_SCREEN
    ) {
        composable(AppDestination.SPLASH_SCREEN) {
            SplashScreen(navController = navController)
        }
        authNavGraph(navController)
        composable(AppDestination.HOME_SCREEN) {
            HomeScreen()
        }
    }
}