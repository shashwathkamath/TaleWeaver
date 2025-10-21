package com.kamath.taleweaver

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kamath.taleweaver.core.util.AppDestination
import com.kamath.taleweaver.login.presentation.LoginScreen
import com.kamath.taleweaver.registration.presentation.NavigationEvent
import com.kamath.taleweaver.registration.presentation.RegistrationScreen
import com.kamath.taleweaver.registration.presentation.RegistrationViewModel
import com.kamath.taleweaver.ui.theme.TaleWeaverTheme
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaleWeaverTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppDestination.REGISTRATION_SCREEN
    ) {
        composable(AppDestination.LOGIN_SCREEN) {
            LoginScreen(onLoginSuccess = { Log.d("MAIN", "Success") })
        }
        composable(AppDestination.REGISTRATION_SCREEN) {
            val viewmodel: RegistrationViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                viewmodel.uiEvent.collect { event ->
                    when (event) {
                        is NavigationEvent.NavigateToLogin -> {
                            navController.navigate(AppDestination.LOGIN_SCREEN) {
                                popUpTo(navController.graph.findStartDestination().id){
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            }
            RegistrationScreen(viewmodel = viewmodel)
        }
    }
}