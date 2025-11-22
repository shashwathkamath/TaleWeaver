package com.kamath.taleweaver.di

import android.content.Context
import com.kamath.taleweaver.ui.theme.SimpleThemeManager
import com.kamath.taleweaver.ui.theme.ThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing theme-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object ThemeModule {

    /**
     * Provides SimpleThemeManager singleton (in-memory, no persistence)
     * Use this for quick implementation without DataStore
     */
    @Provides
    @Singleton
    fun provideSimpleThemeManager(): SimpleThemeManager {
        return SimpleThemeManager()
    }

    /**
     * Provides ThemeManager singleton (with DataStore persistence)
     * Uncomment this and comment out SimpleThemeManager if you want persistent theme storage
     * Requires DataStore dependency in build.gradle
     */
    @Provides
    @Singleton
    fun provideThemeManager(
        @ApplicationContext context: Context
    ): ThemeManager {
        return ThemeManager(context)
    }
}
