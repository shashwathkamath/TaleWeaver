package com.kamath.taleweaver.home.account.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.Constants.USERS_COLLECTION
import com.kamath.taleweaver.home.account.domain.repository.AccountRepository
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
}