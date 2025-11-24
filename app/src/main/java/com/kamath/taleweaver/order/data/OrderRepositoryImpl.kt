package com.kamath.taleweaver.order.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.order.domain.model.Order
import com.kamath.taleweaver.order.domain.model.OrderStatus
import com.kamath.taleweaver.order.domain.repository.OrderRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : OrderRepository {

    override suspend fun createOrder(order: Order): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val orderWithBuyer = order.copy(buyerId = currentUserId)

            val docRef = firestore.collection("orders")
                .add(orderWithBuyer)
                .await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrder(orderId: String): Result<Order> {
        return try {
            val snapshot = firestore.collection("orders")
                .document(orderId)
                .get()
                .await()

            val order = snapshot.toObject(Order::class.java)?.copy(id = snapshot.id)
                ?: return Result.failure(Exception("Order not found"))

            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserOrders(userId: String): Result<List<Order>> {
        return try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("buyerId", userId)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }

            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        return try {
            firestore.collection("orders")
                .document(orderId)
                .update("status", status)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
