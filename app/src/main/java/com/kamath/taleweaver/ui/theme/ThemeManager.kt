//package com.kamath.taleweaver.ui.theme
//
//import android.content.Context
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.map
//import java.util.prefs.Preferences
//import javax.inject.Inject
//import javax.inject.Singleton
//
//private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")
//
///**
// * Theme preferences for TaleWeaver app
// */
//data class ThemePreferences(
//    val isDarkMode: Boolean = false,
//    val useDynamicColors: Boolean = false
//)
//
///**
// * Manager for theme-related preferences
// * Handles dark/light mode toggle and persistence
// */
//@Singleton
//class ThemeManager @Inject constructor(
//    private val context: Context
//) {
//    companion object {
//        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
//        private val DYNAMIC_COLORS_KEY = booleanPreferencesKey("dynamic_colors")
//    }
//
//    /**
//     * Flow of current theme preferences
//     */
//    val themePreferences: Flow<ThemePreferences> = context.dataStore.data.map { preferences ->
//        ThemePreferences(
//            isDarkMode = preferences[DARK_MODE_KEY] ?: false,
//            useDynamicColors = preferences[DYNAMIC_COLORS_KEY] ?: false
//        )
//    }
//
//    /**
//     * Toggle between dark and light mode
//     */
//    suspend fun toggleDarkMode() {
//        context.dataStore.edit { preferences ->
//            val current = preferences[DARK_MODE_KEY] ?: false
//            preferences[DARK_MODE_KEY] = !current
//        }
//    }
//
//    /**
//     * Set dark mode explicitly
//     */
//    suspend fun setDarkMode(enabled: Boolean) {
//        context.dataStore.edit { preferences ->
//            preferences[DARK_MODE_KEY] = enabled
//        }
//    }
//
//    /**
//     * Toggle dynamic colors (Android 12+)
//     */
//    suspend fun toggleDynamicColors() {
//        context.dataStore.edit { preferences ->
//            val current = preferences[DYNAMIC_COLORS_KEY] ?: false
//            preferences[DYNAMIC_COLORS_KEY] = !current
//        }
//    }
//
//    /**
//     * Set dynamic colors explicitly
//     */
//    suspend fun setDynamicColors(enabled: Boolean) {
//        context.dataStore.edit { preferences ->
//            preferences[DYNAMIC_COLORS_KEY] = enabled
//        }
//    }
//}
//
///**
// * Composable extension to easily access theme preferences
// */
//@Composable
//fun ThemeManager.themePreferencesState(): ThemePreferences {
//    val preferences by themePreferences.collectAsState(initial = ThemePreferences())
//    return preferences
//}
