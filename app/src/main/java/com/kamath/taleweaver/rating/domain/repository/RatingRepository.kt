package com.kamath.taleweaver.rating.domain.repository

import com.kamath.taleweaver.rating.domain.model.Rating

interface RatingRepository {
    suspend fun submitRating(rating: Rating): Result<Unit>
    suspend fun getSellerRatings(sellerId: String): Result<List<Rating>>
    suspend fun updateSellerAverageRating(sellerId: String): Result<Unit>
}
