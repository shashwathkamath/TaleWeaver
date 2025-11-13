package com.kamath.taleweaver.registration.domain.usecases

import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.registration.domain.model.RegistrationData
import com.kamath.taleweaver.registration.domain.repository.RegistrationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val repository: RegistrationRepository
) {
    operator fun invoke(registrationData: RegistrationData): Flow<Resource<Unit>> =
        repository.registerUser(registrationData)
}