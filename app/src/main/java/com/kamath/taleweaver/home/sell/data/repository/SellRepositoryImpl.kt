package com.kamath.taleweaver.home.sell.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.Constants.USERS_COLLECTION
import com.kamath.taleweaver.core.util.GeocodingService
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.feed.domain.model.ListingStatus
import com.kamath.taleweaver.home.search.util.ListingGeoHelper
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
            val geoPoint = userProfile?.address?.takeIf { it.isNotBlank() }?.let { address ->
                geocodingService.getGeoPointFromAddress(address)
            }

            // Create Listing object
            val listing = Listing(
                sellerId = user.uid,
                sellerUsername = userProfile?.username ?: user.displayName ?: "Anonymous",
                title = request.title,
                author = request.author,
                isbn = request.isbn,
                genres = request.genres,
                description = request.description,
                userImageUrls = request.userImageUrls,
                coverImageFromApi = request.coverImageFromApi,
                price = request.price,
                originalPrice = request.originalPrice,
                originalPriceCurrency = request.originalPriceCurrency,
                condition = request.condition,
                l = geoPoint,
                shippingOffered = request.shippingOffered,
                status = ListingStatus.AVAILABLE
            )

            // Use ListingGeoHelper to save with proper geohash for GeoFirestore queries
            val result = ListingGeoHelper.saveListing(firestore, listing)
            result.fold(
                onSuccess = { docId ->
                    emit(ApiResult.Success(docId))
                },
                onFailure = { e ->
                    emit(ApiResult.Error(e.message ?: "Failed to create listing"))
                }
            )
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
            // Upload user-selected images to Firebase Storage
            val userUrls = mutableListOf<String>()
            imageUris.forEachIndexed { index, uri ->
                val ref = storage.reference
                    .child("listings/${auth.currentUser?.uid}/${System.currentTimeMillis()}_$index.jpg")
                ref.putFile(uri).await()
                userUrls.add(ref.downloadUrl.await().toString())
            }

            val updatedRequest = request.copy(userImageUrls = userUrls)
            createListing(updatedRequest).collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Failed to create listing"))
        }
    }
}