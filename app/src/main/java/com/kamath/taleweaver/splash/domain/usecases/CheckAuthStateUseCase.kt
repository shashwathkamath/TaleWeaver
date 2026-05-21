package com.kamath.taleweaver.splash.domain.usecases

import com.google.firebase.auth.FirebaseAuth
import com.kamath.taleweaver.core.util.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class AuthState {
    AUTHENTICATED, UNAUTHENTICATED, LOADING
}

class CheckAuthStateUseCase @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend operator fun invoke(): ApiResult<AuthState> {
        // TODO: remove before release — bypasses login for UI testing
        return ApiResult.Success(AuthState.AUTHENTICATED)
    }
}