package com.kamath.taleweaver.home.sell.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.Constants.LISTINGS_COLLECTION
import com.kamath.taleweaver.core.util.Constants.USERS_COLLECTION
import com.kamath.taleweaver.core.util.GeocodingService
import com.kamath.taleweaver.home.feed.domain.model.ListingStatus
import com.kamath.taleweaver.home.sell.domain.model.CreateListingRequest
import com.kamath.taleweaver.home.sell.domain.repository.SellRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SellRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val geocodingService: GeocodingService
) : SellRepository {

    override fun uploadImages(imageUris: List<Uri>): Flow<ApiResult<List<String>>> = flow {
        emit(ApiResult.Loading())
        try {
            val urls = imageUris.mapIndexed { index, uri ->
                val ref = storage.reference
                    .child("listings/${auth.currentUser?.uid}/${System.currentTimeMillis()}_$index.jpg")
                ref.putFile(uri).await()
                ref.downloadUrl.await().toString()
            }
            emit(ApiResult.Success(urls))
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Failed to upload images"))
        }
    }

    override fun createListing(request: CreateListingRequest): Flow<ApiResult<String>> = flow {
        emit(ApiResult.Loading())
        try {
            val user = auth.currentUser ?: throw Exception("User not authenticated")

            // Fetch user profile to get address
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .get()
                .await()
            val userProfile = userDoc.toObject(UserProfile::class.java)

            // Convert user address to GeoPoint for the listing
            val location = userProfile?.address?.takeIf { it.isNotBlank() }?.let { address ->
                geocodingService.getGeoPointFromAddress(address)
            }

            val listing = hashMapOf(
                "title" to request.title,
                "author" to request.author,
                "isbn" to request.isbn,
                "description" to request.description,
                "genres" to request.genres.map { it.name },
                "price" to request.price,
                "originalPrice" to request.originalPrice,
                "originalPriceCurrency" to request.originalPriceCurrency,
                "condition" to request.condition.name,
                "shippingOffered" to request.shippingOffered,
                "location" to location,
                "coverImageUrls" to request.coverImageUrls,
                "sellerId" to user.uid,
                "sellerUsername" to (userProfile?.username ?: user.displayName ?: "Anonymous"),
                "status" to ListingStatus.AVAILABLE.name,
                "createdAt" to FieldValue.serverTimestamp()
            )
            val docRef = firestore.collection(LISTINGS_COLLECTION)
                .add(listing)
                .await()
            emit(ApiResult.Success(docRef.id))
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Failed to create listing"))
        }
    }

    override fun createListingWithImages(
        request: CreateListingRequest,
        imageUris: List<Uri>
    ): Flow<ApiResult<String>> = flow {
        emit(ApiResult.Loading())
        try {
            val urls = mutableListOf<String>()
            imageUris.forEachIndexed { index, uri ->
                val ref = storage.reference
                    .child("listings/${auth.currentUser?.uid}/${System.currentTimeMillis()}_$index.jpg")
                ref.putFile(uri).await()
                urls.add(ref.downloadUrl.await().toString())
            }
            val updatedRequest = request.copy(coverImageUrls = urls)
            createListing(updatedRequest).collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Failed to create listing"))
        }
    }
}