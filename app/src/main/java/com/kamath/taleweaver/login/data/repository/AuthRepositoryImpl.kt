package com.kamath.taleweaver.login.data.repository

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.login.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    override fun loginUser(
        email: String,
        password: String
    ): Flow<ApiResult<AuthResult>> = flow {
        try {
            emit(ApiResult.Loading())
            val result = auth.signInWithEmailAndPassword(
                email, password
            ).await()
            emit(ApiResult.Success(result))
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "An unknown error occurred"))
        }
    }
}