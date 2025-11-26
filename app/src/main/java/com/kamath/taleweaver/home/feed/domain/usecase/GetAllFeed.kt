package com.kamath.taleweaver.home.feed.domain.usecase

import com.google.firebase.firestore.QuerySnapshot
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllFeed @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(genreIds: Set<String> = emptySet()): Flow<ApiResult<QuerySnapshot>> =
        feedRepository.getInitialFeed(genreIds)
}