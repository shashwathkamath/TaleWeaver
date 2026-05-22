package com.taleweaver.app.login.domain.usecases

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.login.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateUserProfileUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(userId: String, email: String, username: String): Flow<ApiResult<Unit>> =
        repository.createUserProfile(userId, email, username)
}
