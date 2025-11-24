package com.kamath.taleweaver.home.account.domain.usecase

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.account.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteListingUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(listingId: String): Flow<ApiResult<Unit>> = repository.deleteListing(listingId)
}
