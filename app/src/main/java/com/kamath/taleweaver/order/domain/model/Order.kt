package com.kamath.taleweaver.order.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.kamath.taleweaver.cart.domain.model.CartItem
import java.util.Date

data class Order(
    @DocumentId val id: String = "",
    val buyerId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    @ServerTimestamp
    val orderDate: Date? = null,
    val estimatedDeliveryDate: Long = 0L, // Timestamp for estimated delivery
    val status: OrderStatus = OrderStatus.PENDING,
    val sellersRated: Map<String, Boolean> = emptyMap() // Track which sellers have been rated
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
