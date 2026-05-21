package com.kamath.taleweaver.di

import com.kamath.taleweaver.home.account.data.repository.AccountRepositoryImpl
import com.kamath.taleweaver.home.account.domain.repository.AccountRepository
import com.kamath.taleweaver.home.feed.data.repository.FeedRepositoryImpl
import com.kamath.taleweaver.home.feed.domain.repository.FeedRepository
import com.kamath.taleweaver.home.listingDetail.data.ListingDetailRepositoryImpl
import com.kamath.taleweaver.home.listingDetail.domain.repository.ListingDetailRepository
import com.kamath.taleweaver.home.search.data.SearchRepositoryImpl
import com.kamath.taleweaver.home.search.domain.repository.SearchRepository
import com.kamath.taleweaver.home.search.util.LocationFacade
import com.kamath.taleweaver.home.search.util.LocationPermissionHandler
import com.kamath.taleweaver.home.sell.data.repository.BookApiRepositoryImpl
import com.kamath.taleweaver.home.sell.data.repository.SellRepositoryImpl
import com.kamath.taleweaver.home.sell.domain.repository.BookApiRepository
import com.kamath.taleweaver.home.sell.domain.repository.SellRepository
import com.kamath.taleweaver.login.data.repository.AuthRepositoryImpl
import com.kamath.taleweaver.login.domain.repository.AuthRepository
import com.kamath.taleweaver.registration.data.repository.RegistrationRepositoryImpl
import com.kamath.taleweaver.registration.domain.repository.RegistrationRepository
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
        searchRepositoryImpl: SearchRepositoryImpl
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
