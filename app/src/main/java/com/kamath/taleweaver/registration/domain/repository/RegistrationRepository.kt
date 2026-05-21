package com.kamath.taleweaver.registration.domain.repository

import com.kamath.taleweaver.core.util.ApiResult
import kotlinx.coroutines.flow.Flow

// Kept for any future profile-management operations bound to this repository.
interface RegistrationRepository {
    fun createUserProfile(userId: String, email: String, username: String): Flow<ApiResult<Unit>>
}
