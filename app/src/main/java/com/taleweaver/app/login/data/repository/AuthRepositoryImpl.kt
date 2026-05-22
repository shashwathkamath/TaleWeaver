package com.taleweaver.app.login.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.login.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun sendOtp(email: String): Flow<ApiResult<Unit>> = flow {
        emit(ApiResult.Loading())
        functions.getHttpsCallable("sendOtp")
            .call(hashMapOf("email" to email))
            .await()
        emit(ApiResult.Success(Unit))
    }.catch { e ->
        Timber.e(e, "sendOtp failed")
        emit(ApiResult.Error(sendOtpError(e)))
    }

    override fun verifyOtpAndSignIn(email: String, code: String): Flow<ApiResult<String>> = flow {
        emit(ApiResult.Loading())
        val result = functions.getHttpsCallable("verifyOtp")
            .call(hashMapOf("email" to email, "code" to code))
            .await()
        @Suppress("UNCHECKED_CAST")
        val token = (result.data as Map<String, Any>)["token"] as String
        val authResult = auth.signInWithCustomToken(token).await()
        emit(ApiResult.Success(authResult.user!!.uid))
    }.catch { e ->
        Timber.e(e, "verifyOtp failed")
        emit(ApiResult.Error(verifyOtpError(e)))
    }

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

    private fun sendOtpError(e: Throwable): String {
        if (e is FirebaseFunctionsException) {
            return when (e.code) {
                FirebaseFunctionsException.Code.NOT_FOUND,
                FirebaseFunctionsException.Code.UNAVAILABLE ->
                    "Could not send code. Please check your connection and try again."
                FirebaseFunctionsException.Code.INVALID_ARGUMENT ->
                    "Please enter a valid email address."
                else -> e.message ?: "Could not send code. Please try again."
            }
        }
        return "Could not send code. Please check your connection and try again."
    }

    private fun verifyOtpError(e: Throwable): String {
        if (e is FirebaseFunctionsException) {
            return when (e.code) {
                FirebaseFunctionsException.Code.NOT_FOUND ->
                    "No code was sent to this email. Please go back and request a new one."
                FirebaseFunctionsException.Code.DEADLINE_EXCEEDED ->
                    "Code expired. Please request a new one."
                FirebaseFunctionsException.Code.RESOURCE_EXHAUSTED ->
                    "Too many incorrect attempts. Please request a new code."
                FirebaseFunctionsException.Code.INVALID_ARGUMENT ->
                    e.message ?: "Incorrect code. Please try again."
                else -> e.message ?: "Something went wrong. Please try again."
            }
        }
        return e.message ?: "Something went wrong. Please try again."
    }
}
