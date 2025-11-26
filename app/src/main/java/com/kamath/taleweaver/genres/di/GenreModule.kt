package com.kamath.taleweaver.genres.di

import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.genres.data.local.GenreDao
import com.kamath.taleweaver.genres.data.repository.GenreRepositoryImpl
import com.kamath.taleweaver.genres.domain.repository.GenreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GenreModule {

    @Provides
    @Singleton
    fun provideGenreRepository(
        genreDao: GenreDao,
        firestore: FirebaseFirestore
    ): GenreRepository {
        return GenreRepositoryImpl(genreDao, firestore)
    }
}
