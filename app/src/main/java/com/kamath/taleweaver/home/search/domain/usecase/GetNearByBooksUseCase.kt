package com.kamath.taleweaver.home.search.domain.usecase

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * A use case that retrieves all book listings within a specified radius of a
 * given geographical location, without a text query.
 *
 * @param repository The repository responsible for fetching search data.
 */
class GetNearByBooksUseCase @Inject constructor(
    private val repository: SearchRepository
) {

    /**
     * Executes the operation to get nearby books.
     *
     * @param latitude The user's current latitude.
     * @param longitude The user's current longitude.
     * @param radiusInKm The search radius in kilometers.
     * @return A Flow emitting the result of the operation, wrapped in an [ApiResult].
     */
    operator fun invoke(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double
    ): Flow<ApiResult<List<Listing>>> {
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0 || radiusInKm <= 0) {
            throw IllegalArgumentException("Invalid geographical parameters provided.")
        }
        return repository.getNearbyBooks(
            latitude = latitude,
            longitude = longitude,
            radiusInKm = radiusInKm
        )
    }
}