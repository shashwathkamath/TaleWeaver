package com.kamath.taleweaver.registration.domain.repository

import com.google.firebase.auth.AuthResult
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.registration.domain.model.User
import kotlinx.coroutines.flow.Flow

interface RegistrationRepository {

    fun signUpUser(
        user: User
    ): Flow<Resource<AuthResult>>
}