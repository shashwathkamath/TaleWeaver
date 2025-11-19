package com.kamath.taleweaver

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.kamath.taleweaver.core.navigation.AppNavigation
import com.kamath.taleweaver.ui.theme.TaleWeaverTheme
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
                AppNavigation()

            }
        }
    }
}