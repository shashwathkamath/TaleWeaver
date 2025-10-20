package com.kamath.taleweaver.login.domain.repository

import com.google.firebase.auth.AuthResult
import com.kamath.taleweaver.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun loginUser(email: String, password: String): Flow<Resource<AuthResult>>
}