package com.taleweaver.app.home.feed.domain.usecase

import com.google.firebase.firestore.QuerySnapshot
import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllFeed @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(genreIds: Set<String> = emptySet()): Flow<ApiResult<QuerySnapshot>> =
        feedRepository.getInitialFeed(genreIds)
}