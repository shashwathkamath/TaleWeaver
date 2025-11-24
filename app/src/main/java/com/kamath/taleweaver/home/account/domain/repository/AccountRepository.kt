package com.kamath.taleweaver.home.account.domain.repository

import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.feed.domain.model.Listing
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    /**
     * Fetches the profile of the currently logged-in user from Firestore.
     */
    fun getUserProfile(): Flow<ApiResult<UserProfile>>

    /**
     * Logs the current user out of Firebase Authentication.
     * @return A Flow that emits ApiResult.Success(Unit) upon completion.
     */
    fun logoutUser(): Flow<ApiResult<Unit>>

    fun updateUserProfile(userProfile: UserProfile): Flow<ApiResult<String>>

    /**
     * Fetches all listings created by the current user.
     */
    fun getUserListings(): Flow<ApiResult<List<Listing>>>

    /**
     * Deletes a listing by its ID. Only the owner can delete their listings.
     */
    fun deleteListing(listingId: String): Flow<ApiResult<Unit>>

    /**
     * Uploads a profile picture and returns the download URL.
     */
    fun uploadProfilePicture(imageUri: android.net.Uri): Flow<ApiResult<String>>
}