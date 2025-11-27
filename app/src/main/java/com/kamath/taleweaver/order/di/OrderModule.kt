package com.kamath.taleweaver.order.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.order.data.OrderRepositoryImpl
import com.kamath.taleweaver.order.domain.repository.OrderRepository
import com.kamath.taleweaver.order.domain.usecase.GenerateShippingLabelUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OrderModule {

    @Provides
    @Singleton
    fun provideOrderRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        generateShippingLabelUseCase: GenerateShippingLabelUseCase
    ): OrderRepository {
        return OrderRepositoryImpl(firestore, auth, generateShippingLabelUseCase)
    }
}
