package com.kamath.taleweaver.di

import android.content.Context
import androidx.room.Room
import com.kamath.taleweaver.genres.data.local.GenreDao
import com.kamath.taleweaver.genres.data.local.GenreDatabase
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
    fun provideGenreDatabase(
        @ApplicationContext context: Context
    ): GenreDatabase {
        return Room.databaseBuilder(
            context,
            GenreDatabase::class.java,
            GenreDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideGenreDao(database: GenreDatabase): GenreDao {
        return database.genreDao()
    }
}
