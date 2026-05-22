package com.taleweaver.app.home.account.domain.usecase

import android.net.Uri
import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.account.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadProfilePictureUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(imageUri: Uri): Flow<ApiResult<String>> = repository.uploadProfilePicture(imageUri)
}
