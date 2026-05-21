package com.taleweaver.app.rating.domain.usecase

import com.taleweaver.app.rating.domain.model.Rating
import com.taleweaver.app.rating.domain.repository.RatingRepository
import javax.inject.Inject

class SubmitRatingUseCase @Inject constructor(
    private val repository: RatingRepository
) {
    suspend operator fun invoke(rating: Rating): Result<Unit> {
        return repository.submitRating(rating)
    }
}
