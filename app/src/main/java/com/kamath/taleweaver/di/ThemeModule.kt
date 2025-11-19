package com.kamath.taleweaver.di

import android.content.Context
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
     * Provides ThemeManager singleton
     */
    @Provides
    @Singleton
    fun provideThemeManager(
        @ApplicationContext context: Context
    ): ThemeManager {
        return ThemeManager(context)
    }
}
