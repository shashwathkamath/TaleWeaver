package com.kamath.taleweaver.cart.domain.usecase

import com.kamath.taleweaver.cart.domain.repository.CartRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetCartItemCountUseCase @Inject constructor(
    private val repository: CartRepository
) {
    operator fun invoke(): Flow<Int> {
        return repository.getCartItemCount()
    }
}
