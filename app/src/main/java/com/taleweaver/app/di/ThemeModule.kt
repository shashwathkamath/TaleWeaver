package com.taleweaver.app.di

import com.taleweaver.app.ui.theme.SimpleThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ThemeModule {

    @Provides
    @Singleton
    fun provideSimpleThemeManager(): SimpleThemeManager {
        return SimpleThemeManager()
    }
}
