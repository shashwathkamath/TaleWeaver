package com.kamath.taleweaver.rating.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.rating.data.RatingRepositoryImpl
import com.kamath.taleweaver.rating.domain.repository.RatingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RatingModule {

    @Provides
    @Singleton
    fun provideRatingRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): RatingRepository {
        return RatingRepositoryImpl(firestore, auth)
    }
}
