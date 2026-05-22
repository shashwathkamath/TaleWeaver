package com.taleweaver.app.home.search.domain.repository

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.search.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    /**
     * Search listings via Algolia. All filtering (text, genre, geo radius) is server-side.
     *
     * @param expandedGenreIds BookGenre enum names to filter by (OR logic). Empty = no genre filter.
     * @param page 0-based page number for pagination.
     */
    fun searchListings(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double,
        query: String = "",
        expandedGenreIds: Set<String> = emptySet(),
        page: Int = 0
    ): Flow<ApiResult<SearchResult>>
}
