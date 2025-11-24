package com.kamath.taleweaver.cart.data

import com.kamath.taleweaver.cart.domain.model.CartItem
import com.kamath.taleweaver.cart.domain.repository.CartRepository
import com.kamath.taleweaver.home.feed.domain.model.Listing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor() : CartRepository {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    override fun getCartItems(): Flow<List<CartItem>> = _cartItems

    override suspend fun addToCart(listing: Listing) {
        val currentItems = _cartItems.value.toMutableList()

        // Check if item already exists in cart
        if (currentItems.none { it.listing.id == listing.id }) {
            currentItems.add(CartItem(listing = listing))
            _cartItems.value = currentItems
        }
    }

    override suspend fun removeFromCart(listingId: String) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.removeAll { it.listing.id == listingId }
        _cartItems.value = currentItems
    }

    override suspend fun clearCart() {
        _cartItems.value = emptyList()
    }

    override fun getCartItemCount(): Flow<Int> = _cartItems.map { it.size }
}
