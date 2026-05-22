package com.taleweaver.app.order.domain.usecase

import com.taleweaver.app.order.domain.model.Order
import com.taleweaver.app.order.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * Use case to get orders where the current user is the buyer
 */
class GetUserPurchasesUseCase @Inject constructor(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Order>> {
        return repository.getUserOrders(userId)
    }
}
