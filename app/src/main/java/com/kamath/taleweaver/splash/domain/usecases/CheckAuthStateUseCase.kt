package com.kamath.taleweaver.splash.domain.usecases

import com.google.firebase.auth.FirebaseAuth
import com.kamath.taleweaver.core.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class AuthState {
    AUTHENTICATED, UNAUTHENTICATED, LOADING
}

class CheckAuthStateUseCase @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend operator fun invoke(): Resource<AuthState> {
        return withContext(Dispatchers.IO) {
            try {
                if (firebaseAuth.currentUser != null) {
                    Resource.Success(AuthState.AUTHENTICATED)
                } else {
                    Resource.Success(AuthState.UNAUTHENTICATED)
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}