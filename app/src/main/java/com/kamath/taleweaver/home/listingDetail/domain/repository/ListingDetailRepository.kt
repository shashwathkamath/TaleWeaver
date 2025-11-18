package com.kamath.taleweaver.home.listingDetail.domain.repository

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.feed.domain.model.Listing
import kotlinx.coroutines.flow.Flow

interface ListingDetailRepository {
    fun getListingById(listingId: String): Flow<ApiResult<Listing>>
}