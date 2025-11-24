package com.kamath.taleweaver.rating.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.rating.domain.model.Rating
import com.kamath.taleweaver.rating.domain.repository.RatingRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RatingRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : RatingRepository {

    override suspend fun submitRating(rating: Rating): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

            val ratingWithBuyer = rating.copy(
                buyerId = currentUserId,
                timestamp = System.currentTimeMillis()
            )

            // Save rating to Firestore
            firestore.collection("ratings")
                .add(ratingWithBuyer)
                .await()

            // Update seller's average rating
            updateSellerAverageRating(rating.sellerId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSellerRatings(sellerId: String): Result<List<Rating>> {
        return try {
            val snapshot = firestore.collection("ratings")
                .whereEqualTo("sellerId", sellerId)
                .get()
                .await()

            val ratings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Rating::class.java)?.copy(id = doc.id)
            }

            Result.success(ratings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSellerAverageRating(sellerId: String): Result<Unit> {
        return try {
            // Get all ratings for this seller
            val ratingsResult = getSellerRatings(sellerId)
            val ratings = ratingsResult.getOrNull() ?: emptyList()

            if (ratings.isEmpty()) {
                return Result.success(Unit)
            }

            // Calculate average
            val averageRating = ratings.map { it.rating }.average().toFloat()
            val ratingCount = ratings.size

            // Update all listings from this seller with new rating
            val listingsSnapshot = firestore.collection("listings")
                .whereEqualTo("sellerId", sellerId)
                .get()
                .await()

            val batch = firestore.batch()
            listingsSnapshot.documents.forEach { doc ->
                batch.update(doc.reference, mapOf(
                    "sellerRating" to averageRating,
                    "sellerRatingCount" to ratingCount
                ))
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
