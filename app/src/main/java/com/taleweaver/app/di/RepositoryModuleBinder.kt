package com.taleweaver.app.di

import com.taleweaver.app.home.account.data.repository.AccountRepositoryImpl
import com.taleweaver.app.home.account.domain.repository.AccountRepository
import com.taleweaver.app.home.feed.data.repository.FeedRepositoryImpl
import com.taleweaver.app.home.feed.domain.repository.FeedRepository
import com.taleweaver.app.home.listingDetail.data.ListingDetailRepositoryImpl
import com.taleweaver.app.home.listingDetail.domain.repository.ListingDetailRepository
import com.taleweaver.app.home.search.data.AlgoliaSearchRepositoryImpl
import com.taleweaver.app.home.search.domain.repository.SearchRepository
import com.taleweaver.app.home.search.util.LocationFacade
import com.taleweaver.app.home.search.util.LocationPermissionHandler
import com.taleweaver.app.home.sell.data.repository.BookApiRepositoryImpl
import com.taleweaver.app.home.sell.data.repository.SellRepositoryImpl
import com.taleweaver.app.home.sell.domain.repository.BookApiRepository
import com.taleweaver.app.home.sell.domain.repository.SellRepository
import com.taleweaver.app.login.data.repository.AuthRepositoryImpl
import com.taleweaver.app.login.domain.repository.AuthRepository
import com.taleweaver.app.registration.data.repository.RegistrationRepositoryImpl
import com.taleweaver.app.registration.domain.repository.RegistrationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModuleBinder {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRegistrationRepository(
        registrationRepositoryImpl: RegistrationRepositoryImpl
    ): RegistrationRepository

    @Binds
    @Singleton
    abstract fun bindFeedRepository(
        feedRepositoryImpl: FeedRepositoryImpl
    ): FeedRepository

    @Binds
    @Singleton
    abstract fun bindListingDetailRepository(
        listingDetailRepositoryImpl: ListingDetailRepositoryImpl
    ): ListingDetailRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(
        accountRepositoryImpl: AccountRepositoryImpl
    ): AccountRepository

    @Binds
    @Singleton
    abstract fun bindLocationFacade(
        locationPermissionHandler: LocationPermissionHandler
    ): LocationFacade

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        algoliaSearchRepositoryImpl: AlgoliaSearchRepositoryImpl
    ): SearchRepository

    @Binds
    @Singleton
    abstract fun bindBookApiRepository(
        bookApiRepository: BookApiRepositoryImpl
    ): BookApiRepository

    @Binds
    @Singleton
    abstract fun bindSellRepository(
        sellRepositoryImpl: SellRepositoryImpl
    ): SellRepository
}
