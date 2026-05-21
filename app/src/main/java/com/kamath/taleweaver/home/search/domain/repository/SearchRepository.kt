package com.kamath.taleweaver.home.search.domain.repository

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.feed.domain.model.Listing
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    /**
     * Fetches all books within [radiusInKm] of the given location.
     * Always called at MAX radius from the ViewModel — genre filtering is done client-side.
     */
    fun getNearbyBooks(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double = 10.0,
        genreIds: Set<String> = emptySet()
    ): Flow<ApiResult<List<Listing>>>
}
