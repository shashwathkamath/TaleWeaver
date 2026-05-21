package com.taleweaver.app.home.account.domain.usecase

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.account.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LogoutUserUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(): Flow<ApiResult<Unit>> = repository.logoutUser()
}