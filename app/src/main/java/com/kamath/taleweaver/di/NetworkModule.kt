package com.kamath.taleweaver.di

import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.home.sell.data.remote.GoogleBooksApi
import com.kamath.taleweaver.home.sell.data.repository.BookCacheRepositoryImpl
import com.kamath.taleweaver.home.sell.domain.repository.BookCacheRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providesRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(GoogleBooksApi.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun providesGoogleBooksApi(retrofit: Retrofit): GoogleBooksApi =
        retrofit.create(GoogleBooksApi::class.java)

    @Provides
    @Singleton
    fun providesBookCacheRepository(
        firestore: FirebaseFirestore
    ): BookCacheRepository = BookCacheRepositoryImpl(firestore)

}