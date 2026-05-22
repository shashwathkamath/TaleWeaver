package com.taleweaver.app.home.feed.domain.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.feed.domain.model.ListingStatus
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun getInitialFeed(genreIds: Set<String> = emptySet()): Flow<ApiResult<QuerySnapshot>>
    fun getMoreFeed(lastVisiblePost: DocumentSnapshot, genreIds: Set<String> = emptySet()): Flow<ApiResult<QuerySnapshot>>
    suspend fun updateListingStatus(listingId: String, status: ListingStatus)
}