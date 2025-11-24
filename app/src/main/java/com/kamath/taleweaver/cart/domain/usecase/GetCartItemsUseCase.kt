package com.kamath.taleweaver.cart.domain.usecase

import com.kamath.taleweaver.cart.domain.model.CartItem
import com.kamath.taleweaver.cart.domain.repository.CartRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetCartItemsUseCase @Inject constructor(
    private val repository: CartRepository
) {
    operator fun invoke(): Flow<List<CartItem>> {
        return repository.getCartItems()
    }
}
