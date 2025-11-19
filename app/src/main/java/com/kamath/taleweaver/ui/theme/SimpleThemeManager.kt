package com.kamath.taleweaver.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple in-memory theme manager
 * Use this if you don't need persistent theme storage
 * For persistent storage, use ThemeManager with DataStore
 */
@Singleton
class SimpleThemeManager @Inject constructor() {
    var isDarkMode by mutableStateOf(false)
        private set

    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
    }

    fun setDarkMode(enabled: Boolean) {
        isDarkMode = enabled
    }
}

/**
 * ViewModel for theme management in screens
 * Example usage in your screens
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val simpleThemeManager: SimpleThemeManager
) : ViewModel() {

    val isDarkMode: Boolean
        get() = simpleThemeManager.isDarkMode

    fun toggleTheme() {
        simpleThemeManager.toggleDarkMode()
    }

    fun setDarkMode(enabled: Boolean) {
        simpleThemeManager.setDarkMode(enabled)
    }
}

/**
 * Composable extension to easily access theme state
 */
@Composable
fun SimpleThemeManager.isDarkModeState(): Boolean {
    return isDarkMode
}
