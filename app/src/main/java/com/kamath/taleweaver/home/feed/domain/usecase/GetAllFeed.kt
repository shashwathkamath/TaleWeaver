package com.kamath.taleweaver.home.feed.domain.usecase

import com.google.firebase.firestore.QuerySnapshot
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAllFeed @Inject constructor(
    private val feedRepository: FeedRepository
) {
    operator fun invoke(): Flow<Resource<QuerySnapshot>> = feedRepository.getInitialFeed()
}