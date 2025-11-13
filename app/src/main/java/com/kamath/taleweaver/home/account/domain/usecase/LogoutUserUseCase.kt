package com.kamath.taleweaver.home.account.domain.usecase

import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.account.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LogoutUserUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> = repository.logoutUser()
}