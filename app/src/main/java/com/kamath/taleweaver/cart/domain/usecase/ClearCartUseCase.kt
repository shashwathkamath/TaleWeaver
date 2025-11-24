package com.kamath.taleweaver.cart.domain.usecase

import com.kamath.taleweaver.cart.domain.repository.CartRepository
import jakarta.inject.Inject

class ClearCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke() {
        repository.clearCart()
    }
}
