package com.kamath.taleweaver.rating.domain.model

import com.kamath.taleweaver.cart.domain.model.CartItem

data class Transaction(
    val id: String = "",
    val buyerId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val sellersRated: Set<String> = emptySet() // Track which sellers have been rated
)
