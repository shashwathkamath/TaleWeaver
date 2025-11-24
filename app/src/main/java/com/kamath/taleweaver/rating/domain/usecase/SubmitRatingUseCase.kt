package com.kamath.taleweaver.rating.domain.usecase

import com.kamath.taleweaver.rating.domain.model.Rating
import com.kamath.taleweaver.rating.domain.repository.RatingRepository
import javax.inject.Inject

class SubmitRatingUseCase @Inject constructor(
    private val repository: RatingRepository
) {
    suspend operator fun invoke(rating: Rating): Result<Unit> {
        return repository.submitRating(rating)
    }
}
