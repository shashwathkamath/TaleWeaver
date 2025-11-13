package com.kamath.taleweaver.home.account.domain.repository

import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    /**
     * Fetches the profile of the currently logged-in user from Firestore.
     */
    fun getUserProfile(): Flow<Resource<UserProfile>>

    /**
     * Logs the current user out of Firebase Authentication.
     * @return A Flow that emits Resource.Success(Unit) upon completion.
     */
    fun logoutUser(): Flow<Resource<Unit>>
}