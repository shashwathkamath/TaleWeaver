package com.kamath.taleweaver.home.account.domain.usecase

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.account.domain.repository.AccountRepository
import com.kamath.taleweaver.home.feed.domain.model.Listing
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserListingsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(): Flow<ApiResult<List<Listing>>> = repository.getUserListings()
}
