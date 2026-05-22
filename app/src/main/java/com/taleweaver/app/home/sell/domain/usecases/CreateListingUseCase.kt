package com.taleweaver.app.home.sell.domain.usecases

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.sell.domain.repository.SellRepository
import com.taleweaver.app.home.sell.domain.model.CreateListingRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateListingUseCase @Inject constructor(
    private val repository: SellRepository
) {
    operator fun invoke(request: CreateListingRequest): Flow<ApiResult<String>> =
        repository.createListing(request)
}