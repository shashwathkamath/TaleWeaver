package com.kamath.taleweaver.order.domain.usecase

import com.kamath.taleweaver.order.domain.model.Order
import com.kamath.taleweaver.order.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * Use case to get orders where the current user is the seller
 */
class GetUserSalesUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Order>> {
        return repository.getUserSales(userId)
    }
}
