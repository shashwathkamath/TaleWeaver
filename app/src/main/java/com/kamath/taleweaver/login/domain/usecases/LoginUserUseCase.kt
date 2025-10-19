package com.kamath.taleweaver.login.domain.usecases

import com.google.firebase.auth.AuthResult
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.login.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Resource<AuthResult>> =
        repository.loginUser(email, password)
}