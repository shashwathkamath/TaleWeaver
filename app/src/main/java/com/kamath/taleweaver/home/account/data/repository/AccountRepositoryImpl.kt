package com.kamath.taleweaver.home.account.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    private val firebaseStore: FirebaseFirestore
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

        Timber.d("Saving profile with location: ${userProfile.address}")

        firebaseStore.collection(USERS_COLLECTION)
            .document(currentUserId)
            .set(userProfile)
            .await()
        emit(ApiResult.Success("Profile updated successfully"))
    }.catch {
        emit(ApiResult.Error(it.message.toString()))
    }

    override fun getUserListings(): Flow<ApiResult<List<Listing>>> = flow {
        emit(ApiResult.Loading())
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            emit(ApiResult.Error("User not logged in"))
            return@flow
        }

        val snapshot = firebaseStore.collection(LISTINGS_COLLECTION)
            .whereEqualTo("sellerId", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        val listings = snapshot.toObjects(Listing::class.java)
        emit(ApiResult.Success(listings))
    }.catch { e ->
        Timber.e(e, "Error fetching user listings")
        emit(ApiResult.Error(e.message ?: "Failed to fetch listings"))
    }
}