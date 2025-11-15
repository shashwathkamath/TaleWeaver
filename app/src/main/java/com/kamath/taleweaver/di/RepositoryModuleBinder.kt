package com.kamath.taleweaver.di

import com.kamath.taleweaver.home.account.data.repository.AccountRepositoryImpl
import com.kamath.taleweaver.home.account.domain.repository.AccountRepository
import com.kamath.taleweaver.home.feed.data.repository.FeedRepositoryImpl
import com.kamath.taleweaver.home.feed.domain.repository.FeedRepository
import com.kamath.taleweaver.home.listingDetail.data.ListingDetailRepositoryImpl
import com.kamath.taleweaver.home.listingDetail.domain.repository.ListingDetailRepository
import com.kamath.taleweaver.home.search.util.LocationFacade
import com.kamath.taleweaver.home.search.util.LocationPermissionHandler
import com.kamath.taleweaver.login.data.repository.AuthRepositoryImpl
import com.kamath.taleweaver.login.domain.repository.AuthRepository
import com.kamath.taleweaver.registration.data.repository.RegistrationRepositoryImpl
import com.kamath.taleweaver.registration.domain.repository.RegistrationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModuleBinder {
    @Binds
    @ViewModelScoped
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @ViewModelScoped
    abstract fun bindRegistrationRepository(
        registrationRepositoryImpl: RegistrationRepositoryImpl
    ): RegistrationRepository

    @Binds
    @ViewModelScoped
    abstract fun bindFeedRepository(
        feedRepositoryImpl: FeedRepositoryImpl
    ): FeedRepository

    @Binds
    @ViewModelScoped
    abstract fun bindListingDetailRepository(
        listingDetailRepositoryImpl: ListingDetailRepositoryImpl
    ): ListingDetailRepository

    @Binds
    @ViewModelScoped
    abstract fun bindAccountRepository(
        accountRepositoryImpl: AccountRepositoryImpl
    ): AccountRepository

    @Binds
    @ViewModelScoped
    abstract fun bindLocationFacade(
        locationPermissionHandler: LocationPermissionHandler
    ): LocationFacade
}