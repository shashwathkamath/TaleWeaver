package com.taleweaver.app.genres.domain.usecase

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.genres.domain.repository.GenreRepository
import javax.inject.Inject

/**
 * Use case for syncing genres from Firestore to local cache
 */
class SyncGenresUseCase @Inject constructor(
    private val repository: GenreRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> {
        // Check if refresh is needed
        if (!repository.needsRefresh()) {
            return ApiResult.Success(Unit)
        }

        // Sync from Firestore
        return repository.syncGenresFromFirestore()
    }

    /**
     * Force sync regardless of cache age
     */
    suspend fun forceSync(): ApiResult<Unit> {
        return repository.syncGenresFromFirestore()
    }
}
