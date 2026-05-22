package com.taleweaver.app.login.domain.usecases

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.login.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VerifyOtpAndSignInUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(email: String, code: String): Flow<ApiResult<String>> =
        repository.verifyOtpAndSignIn(email, code)
}
