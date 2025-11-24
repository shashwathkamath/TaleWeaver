package com.kamath.taleweaver.cart.domain.usecase

import com.kamath.taleweaver.cart.domain.repository.CartRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IsItemInCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    operator fun invoke(listingId: String): Flow<Boolean> {
        return repository.getCartItems().map { cartItems ->
            cartItems.any { it.listing.id == listingId }
        }
    }
}
