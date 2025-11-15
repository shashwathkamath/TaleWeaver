package com.kamath.taleweaver.home.search.domain.usecase

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchNearByBooksUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    /**
     * Executes the search operation.
     *
     * @param query The text to search for in the book titles. If blank, it may return all books in the radius.
     * @param latitude The user's current latitude.
     * @param longitude The user's current longitude.
     * @param radiusInKm The search radius in kilometers.
     * @return A Flow emitting the result of the search operation, wrapped in an [ApiResult].
     */
    operator fun invoke(
        query: String,
        latitude: Double,
        longitude: Double,
        radiusInKm: Double
    ): Flow<ApiResult<List<Listing>>> {
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0 || radiusInKm <= 0) {
            throw IllegalArgumentException("Invalid geographical parameters provided.")
        }
        return repository.searchNearbyBooks(
            query = query.trim(),
            latitude = latitude,
            longitude = longitude,
            radiusInKm = radiusInKm
        )
    }
}