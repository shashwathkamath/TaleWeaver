package com.taleweaver.app.home.search.domain.usecase

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.search.domain.model.SearchResult
import com.taleweaver.app.home.search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class GetNearByBooksUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    operator fun invoke(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double,
        query: String = "",
        expandedGenreIds: Set<String> = emptySet(),
        page: Int = 0
    ): Flow<ApiResult<SearchResult>> {
        Timber.d("Searching listings: query='$query' radius=${radiusInKm}km page=$page genres=$expandedGenreIds")
        require(latitude in -90.0..90.0 && longitude in -180.0..180.0 && radiusInKm > 0) {
            "Invalid geographical parameters"
        }
        return repository.searchListings(
            latitude = latitude,
            longitude = longitude,
            radiusInKm = radiusInKm,
            query = query,
            expandedGenreIds = expandedGenreIds,
            page = page
        )
    }
}
