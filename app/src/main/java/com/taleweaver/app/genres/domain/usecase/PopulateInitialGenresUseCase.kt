package com.taleweaver.app.genres.domain.usecase

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.genres.domain.repository.GenreRepository
import javax.inject.Inject

/**
 * Admin use case for populating initial genres to Firestore
 * Should only be called once during app setup
 */
class PopulateInitialGenresUseCase @Inject constructor(
    private val repository: GenreRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> {
        return repository.populateInitialGenres()
    }
}
