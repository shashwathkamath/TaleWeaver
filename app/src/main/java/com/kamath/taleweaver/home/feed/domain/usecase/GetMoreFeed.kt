package com.kamath.taleweaver.home.feed.domain.usecase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.feed.domain.repository.FeedRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetMoreFeed @Inject constructor(
    private val repository: FeedRepository
) {
    operator fun invoke(lastVisiblePost: DocumentSnapshot): Flow<Resource<QuerySnapshot>> {
        return repository.getMoreFeed(lastVisiblePost)
    }
}