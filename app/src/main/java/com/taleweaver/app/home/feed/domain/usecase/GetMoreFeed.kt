package com.taleweaver.app.home.feed.domain.usecase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.feed.domain.repository.FeedRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetMoreFeed @Inject constructor(
    private val repository: FeedRepository
) {
    operator fun invoke(
        lastVisiblePost: DocumentSnapshot,
        genreIds: Set<String> = emptySet()
    ): Flow<ApiResult<QuerySnapshot>> {
        return repository.getMoreFeed(lastVisiblePost, genreIds)
    }
}