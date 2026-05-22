package com.taleweaver.app.order.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.taleweaver.app.order.data.OrderRepositoryImpl
import com.taleweaver.app.order.domain.repository.OrderRepository
import com.taleweaver.app.order.domain.usecase.GenerateShippingLabelUseCase
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
