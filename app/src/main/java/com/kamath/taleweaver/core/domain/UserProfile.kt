package com.kamath.taleweaver.core.domain

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val userRating: Double = 0.0,
    val description: String = "",
    val address: String = ""
)
