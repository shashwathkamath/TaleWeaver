package com.taleweaver.app.cart.domain.usecase

import com.taleweaver.app.cart.domain.repository.CartRepository
import jakarta.inject.Inject

class RemoveFromCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(listingId: String) {
        repository.removeFromCart(listingId)
    }
}
