package com.taleweaver.app.home.listingDetail.domain.repository

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.feed.domain.model.Listing
import kotlinx.coroutines.flow.Flow

interface ListingDetailRepository {
    fun getListingById(listingId: String): Flow<ApiResult<Listing>>
}