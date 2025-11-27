package com.kamath.taleweaver.order.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a book order in the marketplace.
 * Each order is for a single listing (one book).
 */
data class Order(
    @DocumentId val id: String = "",

    // Listing details
    val listingId: String = "",
    val bookTitle: String = "",
    val bookAuthor: String = "",
    val bookImageUrl: String = "",

    // People involved
    val buyerId: String = "",
    val sellerId: String = "",

    // Pricing
    val bookPrice: Double = 0.0,
    val shippingCost: Double = 0.0,
    val totalAmount: Double = 0.0,

    // Addresses
    val buyerAddress: Address? = null,
    val sellerAddress: Address? = null,

    // Shipping details (manual tracking)
    val trackingNumber: String? = null,
    val courierName: String? = null,
    val shippingLabelUrl: String? = null,  // PDF stored in Firebase Storage

    // Timestamps
    @ServerTimestamp
    val createdAt: Date? = null,
    val paidAt: Long? = null,
    val shippedAt: Long? = null,
    val deliveredAt: Long? = null,

    // Status
    val status: OrderStatus = OrderStatus.PENDING,

    // Ratings
    val isSellerRated: Boolean = false
)

/**
 * Indian address format with all required fields
 */
data class Address(
    val name: String = "",
    val phone: String = "",           // 10-digit mobile number
    val addressLine1: String = "",    // House/flat number, street
    val addressLine2: String = "",    // Area, locality
    val landmark: String = "",        // Nearby landmark (helpful in India)
    val city: String = "",
    val state: String = "",
    val pincode: String = "",         // 6-digit PIN code
    val country: String = "India"
) {
    /**
     * Format address for shipping label display
     */
    fun toFormattedString(): String {
        return buildString {
            appendLine(name)
            appendLine(phone)
            appendLine(addressLine1)
            if (addressLine2.isNotBlank()) appendLine(addressLine2)
            if (landmark.isNotBlank()) appendLine("Near: $landmark")
            appendLine("$city, $state - $pincode")
            appendLine(country)
        }
    }

    /**
     * Validate if address has all required fields
     */
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                phone.length == 10 &&
                addressLine1.isNotBlank() &&
                city.isNotBlank() &&
                state.isNotBlank() &&
                pincode.length == 6
    }
}

enum class OrderStatus {
    PENDING,          // Order created, awaiting payment
    PAID,             // Payment confirmed, label needs to be generated
    LABEL_CREATED,    // Shipping label PDF generated
    SHIPPED,          // Seller marked as shipped with tracking
    DELIVERED,        // Order delivered
    CANCELLED         // Order cancelled
}
