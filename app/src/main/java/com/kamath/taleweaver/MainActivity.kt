package com.kamath.taleweaver

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kamath.taleweaver.core.navigation.AppNavigation
import com.kamath.taleweaver.splash.presentation.AuthViewModel
import com.kamath.taleweaver.ui.theme.TaleWeaverTheme
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Dismiss system splash on first Compose frame — Compose SplashScreen handles the animation
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            TaleWeaverTheme {
                val isDark = isSystemInDarkTheme()
                val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant.toArgb()

                SideEffect {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            lightScrim = surfaceVariant,
                            darkScrim = surfaceVariant
                        ),
                        navigationBarStyle = SystemBarStyle.auto(
                            lightScrim = surfaceVariant,
                            darkScrim = surfaceVariant
                        )
                    )
                }

                val authState by authViewModel.authState.collectAsStateWithLifecycle()
                AppNavigation(authState = authState)
            }
        }
    }
}
