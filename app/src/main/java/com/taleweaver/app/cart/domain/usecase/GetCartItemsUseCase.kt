package com.taleweaver.app.cart.domain.usecase

import com.taleweaver.app.cart.domain.model.CartItem
import com.taleweaver.app.cart.domain.repository.CartRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetCartItemsUseCase @Inject constructor(
    private val repository: CartRepository
) {
    operator fun invoke(): Flow<List<CartItem>> {
        return repository.getCartItems()
    }
}
