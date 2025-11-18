package com.kamath.taleweaver.splash.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kamath.taleweaver.core.navigation.AppDestination
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.splash.domain.usecases.AuthState

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        when (val state = authState) {
            is ApiResult.Success -> {
                when (state.data) {
                    AuthState.AUTHENTICATED -> {
                        navController.navigate(AppDestination.HOME_SCREEN) {
                            popUpTo(AppDestination.SPLASH_SCREEN) { inclusive = true }
                        }
                    }

                    AuthState.UNAUTHENTICATED -> {
                        navController.navigate(AppDestination.AUTH_FLOW) {
                            popUpTo(AppDestination.SPLASH_SCREEN) { inclusive = true }
                        }
                    }

                    AuthState.LOADING, null -> { /* Do nothing */
                    }
                }
            }
            is ApiResult.Error -> {
                state.message?.let {
                    snackbarHostState.showSnackbar(it)
                }
                navController.navigate(AppDestination.AUTH_FLOW) {
                    popUpTo(AppDestination.SPLASH_SCREEN) { inclusive = true }
                }
            }
            is ApiResult.Loading -> {
                // The UI will show its loading indicator while in this state.
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (authState is ApiResult.Loading) {
            CircularProgressIndicator()
        }
    }
}