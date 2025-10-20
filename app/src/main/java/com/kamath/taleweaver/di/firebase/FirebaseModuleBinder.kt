package com.kamath.taleweaver.di.firebase

import com.kamath.taleweaver.login.data.repository.AuthRepositoryImpl
import com.kamath.taleweaver.login.domain.repository.AuthRepository
import com.kamath.taleweaver.signUp.data.repository.RegistrationRepositoryImpl
import com.kamath.taleweaver.signUp.domain.repository.RegistrationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class FirebaseModuleBinder {
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
}