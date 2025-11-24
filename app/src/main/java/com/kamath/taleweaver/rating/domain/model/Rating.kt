package com.kamath.taleweaver.rating.domain.model

data class Rating(
    val id: String = "",
    val sellerId: String = "",
    val buyerId: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val transactionId: String = ""
)
