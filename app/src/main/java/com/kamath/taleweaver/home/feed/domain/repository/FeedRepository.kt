package com.kamath.taleweaver.home.feed.domain.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.kamath.taleweaver.core.util.Resource
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    suspend fun getInitialFeed(): Flow<Resource<QuerySnapshot>>
    //suspend fun getMoreFeed(lastVisibleTake: DocumentSnapshot): Result<QuerySnapshot>
}