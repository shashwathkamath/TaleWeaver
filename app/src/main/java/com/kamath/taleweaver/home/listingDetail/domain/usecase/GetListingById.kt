package com.kamath.taleweaver.home.listingDetail.domain.usecase

import com.kamath.taleweaver.home.listingDetail.domain.repository.ListingDetailRepository
import javax.inject.Inject

class GetListingById @Inject constructor(
    private val repository: ListingDetailRepository
) {
    operator fun invoke(listingId: String) = repository.getListingById(listingId)
}