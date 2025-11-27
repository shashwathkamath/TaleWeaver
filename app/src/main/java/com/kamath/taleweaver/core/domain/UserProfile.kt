package com.kamath.taleweaver.core.domain

import com.google.firebase.firestore.IgnoreExtraProperties
import com.kamath.taleweaver.order.domain.model.Address

@IgnoreExtraProperties
data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val userRating: Double = 0.0,
    val description: String = "",
    val phoneNumber: String = "",          // Added for shipping
    val address: String = "",              // Keep for backward compatibility
    val shippingAddress: Address? = null   // New structured address for shipping labels
)
