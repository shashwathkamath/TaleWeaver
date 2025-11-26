package com.kamath.taleweaver.genres.domain.repository

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.genres.domain.model.Genre
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for genre operations
 */
interface GenreRepository {

    /**
     * Get all genres from local cache (Room)
     * Emits updates when genres change
     */
    fun getGenres(): Flow<List<Genre>>

    /**
     * Sync genres from Firestore to local cache
     * Call this periodically or on app start
     */
    suspend fun syncGenresFromFirestore(): ApiResult<Unit>

    /**
     * Get a specific genre by ID from local cache
     */
    suspend fun getGenreById(id: String): Genre?

    /**
     * Check if local cache needs refresh (older than 15 days)
     */
    suspend fun needsRefresh(): Boolean

    /**
     * Admin function: Populate Firestore with initial genres
     * Only call once during setup
     */
    suspend fun populateInitialGenres(): ApiResult<Unit>
}
