package com.kamath.taleweaver.cart.data

import com.kamath.taleweaver.cart.data.local.CartDao
import com.kamath.taleweaver.cart.data.local.CartEntity
import com.kamath.taleweaver.cart.domain.model.CartItem
import com.kamath.taleweaver.cart.domain.repository.CartRepository
import com.kamath.taleweaver.home.feed.domain.model.Listing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    override fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getAllCartItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addToCart(listing: Listing) {
        // Check if item already exists in cart
        val exists = cartDao.isItemInCart(listing.id)
        if (!exists) {
            val cartItem = CartItem(listing = listing)
            val cartEntity = CartEntity.fromDomain(cartItem)
            cartDao.insertCartItem(cartEntity)
        }
    }

    override suspend fun removeFromCart(listingId: String) {
        cartDao.deleteCartItemByListingId(listingId)
    }

    override suspend fun clearCart() {
        cartDao.deleteAllCartItems()
    }

    override fun getCartItemCount(): Flow<Int> {
        return cartDao.getCartItemCount()
    }
}
