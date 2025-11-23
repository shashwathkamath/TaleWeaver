package com.kamath.taleweaver.home.sell.domain.usecases

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.sell.domain.repository.SellRepository
import com.kamath.taleweaver.home.sell.domain.model.CreateListingRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateListingUseCase @Inject constructor(
    private val repository: SellRepository
) {
    operator fun invoke(request: CreateListingRequest): Flow<ApiResult<String>> =
        repository.createListing(request)
}