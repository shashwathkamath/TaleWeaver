package com.kamath.taleweaver.registration.domain.usecases

import com.google.firebase.auth.AuthResult
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.registration.domain.model.User
import com.kamath.taleweaver.registration.domain.repository.RegistrationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val repository: RegistrationRepository
) {
    operator fun invoke(user: User): Flow<Resource<AuthResult>> = repository.signUpUser(user)
}