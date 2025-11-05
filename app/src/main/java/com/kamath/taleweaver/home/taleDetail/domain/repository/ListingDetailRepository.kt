package com.kamath.taleweaver.home.taleDetail.domain.repository

import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.feed.domain.model.Listing
import kotlinx.coroutines.flow.Flow

interface ListingDetailRepository {
    fun getListingById(listingId: String): Flow<Resource<Listing>>
}