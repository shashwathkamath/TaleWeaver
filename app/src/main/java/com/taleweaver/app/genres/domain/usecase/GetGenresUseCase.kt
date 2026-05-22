package com.taleweaver.app.genres.domain.usecase

import com.taleweaver.app.genres.domain.model.Genre
import com.taleweaver.app.genres.domain.repository.GenreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all genres from local cache
 */
class GetGenresUseCase @Inject constructor(
    private val repository: GenreRepository
) {
    operator fun invoke(): Flow<List<Genre>> {
        return repository.getGenres()
    }
}
