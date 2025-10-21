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
    operator fun invoke(): Flow<Resource<QuerySnapshot>> = flow {
        emit(Resource.Loading())
        try {
            val result = feedRepository.getInitialFeed()
            result.onSuccess { snapshot ->
                emit(Resource.Success(snapshot))
            }.onFailure { exception ->
                emit(Resource.Error(exception.localizedMessage ?: "An unexpected error occurred"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}