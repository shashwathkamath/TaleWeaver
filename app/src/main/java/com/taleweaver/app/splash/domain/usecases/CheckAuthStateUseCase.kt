package com.taleweaver.app.splash.domain.usecases

import com.google.firebase.auth.FirebaseAuth
import com.taleweaver.app.core.util.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class AuthState {
    AUTHENTICATED, UNAUTHENTICATED, LOADING
}

class CheckAuthStateUseCase @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend operator fun invoke(): ApiResult<AuthState> = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            ApiResult.Success(AuthState.AUTHENTICATED)
        } else {
            ApiResult.Success(AuthState.UNAUTHENTICATED)
        }
    }
}