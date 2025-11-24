package com.kamath.taleweaver.home.feed.domain.usecase

import com.kamath.taleweaver.home.feed.domain.model.ListingStatus
import com.kamath.taleweaver.home.feed.domain.repository.FeedRepository
import javax.inject.Inject

class UpdateListingStatusUseCase @Inject constructor(
    private val repository: FeedRepository
) {
    suspend operator fun invoke(listingId: String, status: ListingStatus): Result<Unit> {
        return try {
            repository.updateListingStatus(listingId, status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
