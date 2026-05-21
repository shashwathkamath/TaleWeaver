package com.kamath.taleweaver.registration.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.registration.domain.repository.RegistrationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class RegistrationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : RegistrationRepository {

    override fun createUserProfile(
        userId: String,
        email: String,
        username: String
    ): Flow<ApiResult<Unit>> = flow {
        emit(ApiResult.Loading())
        val profile = mapOf(
            "userId" to userId,
            "username" to username,
            "email" to email,
            "profilePictureUrl" to "",
            "userRating" to 0.0,
            "description" to "",
            "address" to ""
        )
        firestore.collection("users").document(userId).set(profile).await()
        emit(ApiResult.Success(Unit))
    }.catch { e ->
        Timber.e(e, "createUserProfile failed")
        emit(ApiResult.Error(e.message ?: "Failed to create profile"))
    }
}
