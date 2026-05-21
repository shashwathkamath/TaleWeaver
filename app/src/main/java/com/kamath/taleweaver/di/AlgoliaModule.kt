package com.kamath.taleweaver.di

import com.kamath.taleweaver.BuildConfig
import com.kamath.taleweaver.home.search.data.remote.AlgoliaApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlgoliaModule {

    @Provides
    @Singleton
    @Named("algolia")
    fun provideAlgoliaOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .addHeader("X-Algolia-Application-Id", BuildConfig.ALGOLIA_APP_ID)
                        .addHeader("X-Algolia-API-Key", BuildConfig.ALGOLIA_SEARCH_API_KEY)
                        .build()
                )
            }
            .build()

    @Provides
    @Singleton
    fun provideAlgoliaApi(@Named("algolia") client: OkHttpClient): AlgoliaApi =
        Retrofit.Builder()
            .baseUrl("https://${BuildConfig.ALGOLIA_APP_ID}-dsn.algolia.net")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AlgoliaApi::class.java)
}
