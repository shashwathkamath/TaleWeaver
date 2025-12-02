package com.kamath.taleweaver.home.account.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.Constants.LISTINGS_COLLECTION
import com.kamath.taleweaver.core.util.Constants.USERS_COLLECTION
import com.kamath.taleweaver.home.account.domain.repository.AccountRepository
import com.kamath.taleweaver.home.feed.domain.model.Listing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : AccountRepository {
    override fun getUserProfile(): Flow<ApiResult<UserProfile>> = flow {
        emit(ApiResult.Loading())
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            emit(ApiResult.Error("User not logged in"))
            return@flow
        }
        val documentSnapshot = firebaseStore
            .collection(USERS_COLLECTION)
            .document(currentUserId)
            .get()
            .await()
        val userProfile = documentSnapshot.toObject(UserProfile::class.java)
        if (userProfile != null) {
            emit(ApiResult.Success(userProfile))
        } else {
            emit(ApiResult.Error("Couldn't fetch user profile"))
        }
    }.catch { e ->
        emit(
            ApiResult.Error(
                e.message.toString()
            )
        )
    }

    override fun logoutUser(): Flow<ApiResult<Unit>> = flow {
        emit(ApiResult.Loading())
        firebaseAuth.signOut()
        emit(ApiResult.Success(Unit))
    }.catch { e ->
        emit(ApiResult.Error(e.message.toString()))
    }

    override fun updateUserProfile(userProfile: UserProfile): Flow<ApiResult<String>> = flow {
        emit(ApiResult.Loading())
        val currentUserId = firebaseAuth.currentUser?.uid
        Timber.d("Current User $currentUserId")
        if (currentUserId == null) {
            emit(ApiResult.Error("Login again to update the profile"))
            return@flow
        }

        Timber.d("Saving profile with address: ${userProfile.address}")
        Timber.d("Shipping address: ${userProfile.shippingAddress}")
        Timber.d("User name: ${userProfile.name}")

        // Build update map with all user profile fields
        val updates = hashMapOf<String, Any?>(
            "userId" to userProfile.userId,
            "username" to userProfile.username,
            "email" to userProfile.email,
            "profilePictureUrl" to userProfile.profilePictureUrl,
            "userRating" to userProfile.userRating,
            "description" to userProfile.description,
            "phoneNumber" to userProfile.phoneNumber,
            "name" to userProfile.name,
            "address" to userProfile.address
        )

        // Add individual shipping address fields
        userProfile.shippingAddress?.let { address ->
            // Use name from user profile
            val recipientName = userProfile.name

            // Save all individual fields (Google Places API already provides structured data)
            updates["shippingAddress.name"] = recipientName
            updates["shippingAddress.phone"] = address.phone
            updates["shippingAddress.unitNumber"] = address.unitNumber
            updates["shippingAddress.addressLine1"] = address.addressLine1
            updates["shippingAddress.addressLine2"] = address.addressLine2
            updates["shippingAddress.landmark"] = address.landmark
            updates["shippingAddress.city"] = address.city
            updates["shippingAddress.state"] = address.state
            updates["shippingAddress.pincode"] = address.pincode
            updates["shippingAddress.country"] = address.country

            Timber.d("Saving individual address fields to Firestore:")
            Timber.d("  name=$recipientName, phone=${address.phone}")
            Timber.d("  unit=${address.unitNumber}, line1=${address.addressLine1}, line2=${address.addressLine2}")
            Timber.d("  city=${address.city}, state=${address.state}, pincode=${address.pincode}, country=${address.country}")
        } ?: run {
            // If shipping address is null, clear all shipping address fields
            updates["shippingAddress"] = null
        }

        firebaseStore.collection(USERS_COLLECTION)
            .document(currentUserId)
            .update(updates)
            .await()
        emit(ApiResult.Success("Profile updated successfully"))
    }.catch {
        emit(ApiResult.Error(it.message.toString()))
    }

    override fun getUserListings(): Flow<ApiResult<List<Listing>>> = flow {
        emit(ApiResult.Loading())
        val currentUserId = firebaseAuth.currentUser?.uid
        Timber.d("Fetching listings for user: $currentUserId")
        if (currentUserId == null) {
            emit(ApiResult.Error("User not logged in"))
            return@flow
        }

        try {
            val snapshot = firebaseStore.collection(LISTINGS_COLLECTION)
                .whereEqualTo("sellerId", currentUserId)
                .get()
                .await()

            Timber.d("Found ${snapshot.documents.size} listings for user")
            val listings = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Listing::class.java)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse listing: ${doc.id}")
                    null
                }
            }.sortedByDescending { it.createdAt }

            emit(ApiResult.Success(listings))
        } catch (e: Exception) {
            Timber.e(e, "Error fetching user listings")
            emit(ApiResult.Error(e.message ?: "Failed to fetch listings"))
        }
    }.catch { e ->
        Timber.e(e, "Error fetching user listings")
        emit(ApiResult.Error(e.message ?: "Failed to fetch listings"))
    }

    override fun deleteListing(listingId: String): Flow<ApiResult<Unit>> = flow {
        emit(ApiResult.Loading())
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            emit(ApiResult.Error("User not logged in"))
            return@flow
        }

        // First verify the listing belongs to the current user
        val listingDoc = firebaseStore.collection(LISTINGS_COLLECTION)
            .document(listingId)
            .get()
            .await()

        val listing = listingDoc.toObject(Listing::class.java)
        if (listing == null) {
            emit(ApiResult.Error("Listing not found"))
            return@flow
        }

        if (listing.sellerId != currentUserId) {
            emit(ApiResult.Error("You can only delete your own listings"))
            return@flow
        }

        // Delete the listing
        firebaseStore.collection(LISTINGS_COLLECTION)
            .document(listingId)
            .delete()
            .await()

        emit(ApiResult.Success(Unit))
    }.catch { e ->
        Timber.e(e, "Error deleting listing")
        emit(ApiResult.Error(e.message ?: "Failed to delete listing"))
    }

    override fun uploadProfilePicture(imageUri: Uri): Flow<ApiResult<String>> = flow {
        emit(ApiResult.Loading())
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            emit(ApiResult.Error("User not logged in"))
            return@flow
        }

        // Upload to Firebase Storage
        val storageRef = firebaseStorage.reference
            .child("profile_pictures/$currentUserId.jpg")

        storageRef.putFile(imageUri).await()
        val downloadUrl = storageRef.downloadUrl.await().toString()

        // Update user profile with new picture URL
        firebaseStore.collection(USERS_COLLECTION)
            .document(currentUserId)
            .update("profilePictureUrl", downloadUrl)
            .await()

        emit(ApiResult.Success(downloadUrl))
    }.catch { e ->
        Timber.e(e, "Error uploading profile picture")
        emit(ApiResult.Error(e.message ?: "Failed to upload profile picture"))
    }
}