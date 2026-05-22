package com.taleweaver.app.home.account.domain.usecase

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.account.domain.repository.AccountRepository
import com.taleweaver.app.home.feed.domain.model.Listing
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserListingsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(): Flow<ApiResult<List<Listing>>> = repository.getUserListings()
}
