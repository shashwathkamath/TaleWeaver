package com.kamath.taleweaver.order.domain.usecase

import com.kamath.taleweaver.order.domain.model.Order
import com.kamath.taleweaver.order.domain.repository.OrderRepository
import javax.inject.Inject

class CreateOrderUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(order: Order): Result<String> {
        return repository.createOrder(order)
    }
}
