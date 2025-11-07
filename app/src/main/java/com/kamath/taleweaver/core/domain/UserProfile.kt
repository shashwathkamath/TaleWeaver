package com.kamath.taleweaver.core.domain


data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val userRating: Double = 0.0,
    val description: String = "",
    val location: Map<String, String> = emptyMap()
)
