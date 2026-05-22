package com.taleweaver.app.cart.domain.repository

import com.taleweaver.app.cart.domain.model.CartItem
import com.taleweaver.app.home.feed.domain.model.Listing
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    suspend fun addToCart(listing: Listing)
    suspend fun removeFromCart(listingId: String)
    suspend fun clearCart()
    fun getCartItemCount(): Flow<Int>
}
