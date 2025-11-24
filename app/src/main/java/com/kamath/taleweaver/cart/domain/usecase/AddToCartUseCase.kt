package com.kamath.taleweaver.cart.domain.usecase

import com.kamath.taleweaver.cart.domain.repository.CartRepository
import com.kamath.taleweaver.home.feed.domain.model.Listing
import jakarta.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(listing: Listing) {
        repository.addToCart(listing)
    }
}
