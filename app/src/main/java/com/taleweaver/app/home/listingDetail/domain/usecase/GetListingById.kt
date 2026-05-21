package com.taleweaver.app.home.listingDetail.domain.usecase

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.feed.domain.model.Listing
import com.taleweaver.app.home.listingDetail.domain.repository.ListingDetailRepository
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