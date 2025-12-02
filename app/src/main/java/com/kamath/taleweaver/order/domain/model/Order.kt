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
 * Universal address format with all required fields
 * Supports both Indian (pincode) and international (zipcode) formats
 * Note: Name is stored separately in UserProfile, not in the address object
 */
data class Address(
    val phone: String = "",           // Phone number
    val unitNumber: String = "",      // Unit/Apartment number
    val addressLine1: String = "",    // Street address (with autocomplete)
    val addressLine2: String = "",    // Additional address info (optional)
    val landmark: String = "",        // Nearby landmark
    val city: String = "",
    val state: String = "",
    val pincode: String = "",         // Postal code (pincode for India, zipcode for USA, etc.)
    val country: String = "India"
) {
    /**
     * Format address for shipping label display
     * Note: Name should be passed separately and added to the output
     */
    fun toFormattedString(name: String = ""): String {
        return buildString {
            if (name.isNotBlank()) appendLine(name)
            if (phone.isNotBlank()) appendLine(phone)
            if (unitNumber.isNotBlank()) appendLine("Unit $unitNumber")
            appendLine(addressLine1)
            if (addressLine2.isNotBlank()) appendLine(addressLine2)
            if (landmark.isNotBlank()) appendLine("Near: $landmark")
            appendLine("$city, $state - $pincode")
            appendLine(country)
        }
    }

    /**
     * Validate if address has all required fields
     * Note: Postal code length varies by country (5 for USA, 6 for India, etc.)
     * Name should be validated separately from UserProfile
     */
    fun isValid(): Boolean {
        return phone.isNotBlank() &&
                addressLine1.isNotBlank() &&
                city.isNotBlank() &&
                state.isNotBlank() &&
                pincode.isNotBlank() &&
                pincode.length >= 4  // Minimum length for most postal codes
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
