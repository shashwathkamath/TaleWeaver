package com.kamath.taleweaver.login.domain.usecases

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.login.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateUserProfileUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(userId: String, email: String, username: String): Flow<ApiResult<Unit>> =
        repository.createUserProfile(userId, email, username)
}
