package com.kamath.taleweaver.cart.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Cart table
 */
@Dao
interface CartDao {

    /**
     * Get all cart items ordered by when they were added (newest first)
     */
    @Query("SELECT * FROM cart_items ORDER BY addedAt DESC")
    fun getAllCartItems(): Flow<List<CartEntity>>

    /**
     * Get a specific cart item by listing ID
     */
    @Query("SELECT * FROM cart_items WHERE listingId = :listingId")
    suspend fun getCartItemByListingId(listingId: String): CartEntity?

    /**
     * Insert or update a cart item (replaces on conflict)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartEntity)

    /**
     * Delete a cart item by listing ID
     */
    @Query("DELETE FROM cart_items WHERE listingId = :listingId")
    suspend fun deleteCartItemByListingId(listingId: String)

    /**
     * Delete all cart items (used when clearing cart)
     */
    @Query("DELETE FROM cart_items")
    suspend fun deleteAllCartItems()

    /**
     * Get cart item count
     */
    @Query("SELECT COUNT(*) FROM cart_items")
    fun getCartItemCount(): Flow<Int>

    /**
     * Check if item exists in cart
     */
    @Query("SELECT EXISTS(SELECT 1 FROM cart_items WHERE listingId = :listingId)")
    suspend fun isItemInCart(listingId: String): Boolean
}
