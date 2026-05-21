package com.taleweaver.app.cart.domain.usecase

import com.taleweaver.app.cart.domain.repository.CartRepository
import com.taleweaver.app.home.feed.domain.model.Listing
import jakarta.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(listing: Listing) {
        repository.addToCart(listing)
    }
}
