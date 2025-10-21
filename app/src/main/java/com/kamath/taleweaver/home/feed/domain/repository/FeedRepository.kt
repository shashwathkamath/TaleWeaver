package com.kamath.taleweaver.home.feed.domain.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

interface FeedRepository {
    suspend fun getInitialFeed(): Result<QuerySnapshot>
    suspend fun getMoreFeed(lastVisibleTake: DocumentSnapshot): Result<QuerySnapshot>
}