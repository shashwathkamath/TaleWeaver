package com.kamath.taleweaver.di

import com.kamath.taleweaver.home.sell.data.remote.GoogleBooksApi
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

}