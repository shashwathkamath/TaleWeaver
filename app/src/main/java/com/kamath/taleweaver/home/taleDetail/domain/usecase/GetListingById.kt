package com.kamath.taleweaver.home.taleDetail.domain.usecase

import com.kamath.taleweaver.home.taleDetail.domain.repository.ListingDetailRepository
import javax.inject.Inject

class GetListingById @Inject constructor(
    private val repository: ListingDetailRepository
) {
    operator fun invoke(listingId: String) = repository.getListingById(listingId)
}