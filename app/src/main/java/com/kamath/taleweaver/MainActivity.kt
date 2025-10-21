package com.kamath.taleweaver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.kamath.taleweaver.registration.presentation.RegistrationScreen
import com.kamath.taleweaver.ui.theme.TaleWeaverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaleWeaverTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    //LoginScreen(onLoginSuccess = { Log.d("MainActivity", "Login successful") })
                    RegistrationScreen()
                }
            }
        }
    }
}