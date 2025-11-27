package com.kamath.taleweaver.order.domain.usecase

import com.kamath.taleweaver.order.domain.model.Order
import com.kamath.taleweaver.order.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * Use case to create an order with addresses and shipping label
 * Business logic is handled in the repository
 */
class CreateOrderUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(order: Order): Result<String> {
        return repository.createOrderWithShippingLabel(order)
    }
}
