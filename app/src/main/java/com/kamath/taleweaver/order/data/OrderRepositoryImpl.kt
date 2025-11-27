package com.kamath.taleweaver.order.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.order.domain.model.Address
import com.kamath.taleweaver.order.domain.model.Order
import com.kamath.taleweaver.order.domain.model.OrderStatus
import com.kamath.taleweaver.order.domain.repository.OrderRepository
import com.kamath.taleweaver.order.domain.usecase.GenerateShippingLabelUseCase
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val generateShippingLabelUseCase: GenerateShippingLabelUseCase
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

    override suspend fun createOrderWithShippingLabel(order: Order): Result<String> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not logged in"))

            // Fetch buyer address (current user)
            val buyerProfile = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()
                .toObject(UserProfile::class.java)

            // Fetch seller address
            val sellerProfile = firestore.collection("users")
                .document(order.sellerId)
                .get()
                .await()
                .toObject(UserProfile::class.java)

            // Get addresses (use shippingAddress if available, otherwise create from old address field)
            val buyerAddress = buyerProfile?.shippingAddress ?: run {
                // Fallback: create Address from old string address field
                val oldAddress = buyerProfile?.address
                if (oldAddress.isNullOrBlank()) {
                    return Result.failure(Exception("Please add your shipping address in profile"))
                }
                Address(
                    name = buyerProfile.username,
                    phone = buyerProfile.phoneNumber.takeIf { it.isNotBlank() } ?: "",
                    addressLine1 = oldAddress,
                    addressLine2 = "",
                    landmark = "",
                    city = "",
                    state = "",
                    pincode = ""
                )
            }

            val sellerAddress = sellerProfile?.shippingAddress ?: run {
                // Fallback: create Address from old string address field
                val oldAddress = sellerProfile?.address
                if (oldAddress.isNullOrBlank()) {
                    return Result.failure(Exception("Seller has not added shipping address yet"))
                }
                Address(
                    name = sellerProfile.username,
                    phone = sellerProfile.phoneNumber.takeIf { it.isNotBlank() } ?: "",
                    addressLine1 = oldAddress,
                    addressLine2 = "",
                    landmark = "",
                    city = "",
                    state = "",
                    pincode = ""
                )
            }

            // Create order with addresses
            val orderWithAddresses = order.copy(
                buyerId = currentUserId,
                buyerAddress = buyerAddress,
                sellerAddress = sellerAddress,
                status = OrderStatus.PAID  // Assuming payment is done before order creation
            )

            // Save order to Firestore first
            val docRef = firestore.collection("orders")
                .add(orderWithAddresses)
                .await()

            val orderId = docRef.id

            // Generate shipping label
            Timber.d("Generating shipping label for order: $orderId")
            when (val labelResult = generateShippingLabelUseCase(orderWithAddresses.copy(id = orderId))) {
                is ApiResult.Success -> {
                    // Update order with label URL
                    docRef.update(
                        mapOf(
                            "shippingLabelUrl" to labelResult.data,
                            "status" to OrderStatus.LABEL_CREATED.name
                        )
                    ).await()

                    Timber.d("Shipping label generated and saved: ${labelResult.data}")
                }
                is ApiResult.Error -> {
                    Timber.e("Failed to generate label: ${labelResult.message}")
                    // Order still created, label can be generated later
                }
                else -> {}
            }

            Result.success(orderId)

        } catch (e: Exception) {
            Timber.e(e, "Error creating order with shipping label")
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
            }.sortedByDescending { it.createdAt }

            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserSales(userId: String): Result<List<Order>> {
        return try {
            val snapshot = firestore.collection("orders")
                .whereEqualTo("sellerId", userId)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.createdAt }

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
