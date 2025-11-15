package com.kamath.taleweaver.home.listingDetail.domain.usecase

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.listingDetail.domain.repository.ListingDetailRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetListingById @Inject constructor(
    private val repository: ListingDetailRepository
) {
    operator fun invoke(listingId: String): Flow<ApiResult<Listing>> {
        require(listingId.isNotBlank()) { "listingId must not be blank" }
        return repository.getListingById(listingId)
    }
}