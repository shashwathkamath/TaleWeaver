package com.kamath.taleweaver.login.domain.repository

import com.kamath.taleweaver.core.util.ApiResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun sendOtp(email: String): Flow<ApiResult<Unit>>
    // Returns the Firebase UID on success
    fun verifyOtpAndSignIn(email: String, code: String): Flow<ApiResult<String>>
    fun createUserProfile(userId: String, email: String, username: String): Flow<ApiResult<Unit>>
}
