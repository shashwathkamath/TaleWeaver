package com.taleweaver.app

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
import com.taleweaver.app.core.navigation.AppNavigation
import com.taleweaver.app.splash.presentation.AuthViewModel
import com.taleweaver.app.ui.theme.TaleWeaverTheme
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Fade the system splash out over 300ms instead of cutting instantly,
        // so the Compose BookCanvas animation plays underneath during the crossfade.
        installSplashScreen().setOnExitAnimationListener { provider ->
            provider.view.animate()
                .alpha(0f)
                .setDuration(300L)
                .withEndAction { provider.remove() }
                .start()
        }

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
