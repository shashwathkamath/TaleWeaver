package com.kamath.taleweaver.signUp.domain.repository

import com.google.firebase.auth.AuthResult
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.signUp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface RegistrationRepository {

    fun signUpUser(
        user: User
    ): Flow<Resource<AuthResult>>
}