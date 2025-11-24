package com.kamath.taleweaver.order.domain.repository

import com.kamath.taleweaver.order.domain.model.Order

interface OrderRepository {
    suspend fun createOrder(order: Order): Result<String> // Returns order ID
    suspend fun getOrder(orderId: String): Result<Order>
    suspend fun getUserOrders(userId: String): Result<List<Order>>
    suspend fun updateOrderStatus(orderId: String, status: com.kamath.taleweaver.order.domain.model.OrderStatus): Result<Unit>
}
