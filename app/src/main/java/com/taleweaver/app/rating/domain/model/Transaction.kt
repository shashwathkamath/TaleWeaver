package com.taleweaver.app.rating.domain.model

import com.taleweaver.app.cart.domain.model.CartItem

data class Transaction(
    val id: String = "",
    val buyerId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val sellersRated: Set<String> = emptySet() // Track which sellers have been rated
)
