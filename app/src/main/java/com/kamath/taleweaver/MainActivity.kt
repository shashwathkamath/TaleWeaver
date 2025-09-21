package com.kamath.taleweaver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kamath.taleweaver.login.presentation.screens.LoginScreen
import com.kamath.taleweaver.signup.presentation.screens.SignUpScreen
import com.kamath.taleweaver.ui.theme.TaleWeaverTheme
import com.kamath.taleweaver.util.routes.Destinations
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaleWeaverTheme {
                AppScreen()
            }
        }
    }
}

@Composable
fun AppScreen() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Destinations.SIGN_UP_SCREEN) {
        composable(Destinations.SIGN_UP_SCREEN) {
            SignUpScreen(
                navigateToLogin = {
                    navController.navigate(Destinations.LOGIN_SCREEN){
                        popUpTo(Destinations.SIGN_UP_SCREEN)
                    }
                })
        }
        composable(Destinations.LOGIN_SCREEN) {
            LoginScreen()
        }
    }
}