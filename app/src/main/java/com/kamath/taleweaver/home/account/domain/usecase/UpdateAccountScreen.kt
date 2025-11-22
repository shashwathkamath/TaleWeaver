package com.kamath.taleweaver.home.account.domain.usecase

import com.kamath.taleweaver.core.domain.UserProfile
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.account.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateAccountScreen @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(userProfile: UserProfile): Flow<ApiResult<String>> =
        repository.updateUserProfile(userProfile = userProfile)
}