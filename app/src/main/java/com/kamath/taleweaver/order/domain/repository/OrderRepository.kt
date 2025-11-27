package com.kamath.taleweaver.order.domain.repository

import com.kamath.taleweaver.order.domain.model.Order

interface OrderRepository {
    suspend fun createOrder(order: Order): Result<String> // Returns order ID

    /**
     * Creates order, fetches buyer/seller addresses, generates shipping label
     * This is the main method that should be used for creating orders
     */
    suspend fun createOrderWithShippingLabel(order: Order): Result<String>

    suspend fun getOrder(orderId: String): Result<Order>
    suspend fun getUserOrders(userId: String): Result<List<Order>> // Orders where user is buyer
    suspend fun getUserSales(userId: String): Result<List<Order>> // Orders where user is seller
    suspend fun updateOrderStatus(orderId: String, status: com.kamath.taleweaver.order.domain.model.OrderStatus): Result<Unit>
}
