package com.taleweaver.app.di

import android.content.Context
import androidx.room.Room
import com.taleweaver.app.cart.data.local.CartDao
import com.taleweaver.app.genres.data.local.GenreDao
import com.taleweaver.app.genres.data.local.TaleWeaverDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTaleWeaverDatabase(
        @ApplicationContext context: Context
    ): TaleWeaverDatabase {
        return Room.databaseBuilder(
            context,
            TaleWeaverDatabase::class.java,
            TaleWeaverDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Allow database to be recreated on version changes
            .build()
    }

    @Provides
    @Singleton
    fun provideGenreDao(database: TaleWeaverDatabase): GenreDao {
        return database.genreDao()
    }

    @Provides
    @Singleton
    fun provideCartDao(database: TaleWeaverDatabase): CartDao {
        return database.cartDao()
    }
}
