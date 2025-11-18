package com.kamath.taleweaver.home.search.domain.repository

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.feed.domain.model.Listing
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    /**
     * Searches for books near a given location that match a specific query (title, author, or genre).
     *
     * @param query The search term (book title, genre, etc.).
     * @param latitude The user's current latitude.
     * @param longitude The user's current longitude.
     * @param radiusInKm The search radius in kilometers.
     * @return A Flow emitting a result containing a list of matching books.
     */
    fun searchNearbyBooks(
        query: String,
        latitude: Double,
        longitude: Double,
        radiusInKm: Double = 10.0
    ): Flow<ApiResult<List<Listing>>>

    /**
     * Fetches all books available within a certain radius of the user's location when the search query is empty.
     *
     * @param latitude The user's current latitude.
     * @param longitude The user's current longitude.
     * @param radiusInKm The search radius in kilometers.
     * @return A Flow emitting a result containing a list of nearby books.
     */
    fun getNearbyBooks(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double = 10.0
    ): Flow<ApiResult<List<Listing>>>
}