package com.kamath.taleweaver.registration.data.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.registration.domain.model.RegistrationData
import com.kamath.taleweaver.registration.domain.repository.RegistrationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class RegistrationRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : RegistrationRepository {


    @OptIn(ExperimentalCoroutinesApi::class)
    override fun registerUser(registrationData: RegistrationData): Flow<ApiResult<Unit>> {
        return signUpUser(registrationData).flatMapConcat { authResult ->
            when (authResult) {
                is ApiResult.Loading -> flow { emit(ApiResult.Loading()) }
                is ApiResult.Success -> {
                    val userId = authResult.data?.user?.uid
                    if (userId != null) {
                        createUserProfile(
                            userId = userId,
                            email = registrationData.email,
                            username = registrationData.username
                        )
                    } else {
                        flow { emit(ApiResult.Error("Auth succeeded but failed to get user id")) }
                    }
                }

                is ApiResult.Error -> {
                    flow { emit(ApiResult.Error(authResult.message.toString())) }
                }
            }
        }
    }

    override fun signUpUser(
        registrationData: RegistrationData
    ): Flow<ApiResult<AuthResult>> = flow {
        emit(ApiResult.Loading())
        val result = firebaseAuth.createUserWithEmailAndPassword(
            registrationData.email,
            registrationData.password
        )
            .await()
        emit(ApiResult.Success(result))
    }.catch { e ->
        Timber.d("Error while registration: ${e.message}")
        emit(ApiResult.Error(e.message.toString()))
    }

    override fun createUserProfile(
        userId: String,
        email: String,
        username: String
    ): Flow<ApiResult<Unit>> = flow {
        emit(ApiResult.Loading())
        val userProfileMap = mapOf(
            "userId" to userId,
            "username" to username,
            "email" to email,
            "profilePictureUrl" to "",
            "userRating" to 0.0,
            "description" to "",
            "address" to ""
        )
        firestore.collection("users")
            .document(userId)
            .set(userProfileMap)
            .await()
        emit(ApiResult.Success(Unit))
    }.catch { e ->
        Timber.d("Error while create user profile: ${e.message}")
        emit(ApiResult.Error(e.message.toString()))
    }
}