package com.taleweaver.app.home.sell.domain.usecases

import android.net.Uri
import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.sell.domain.repository.SellRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadistingImagesUseCase @Inject constructor(
    private val repository: SellRepository
) {
    operator fun invoke(imageUris: List<Uri>): Flow<ApiResult<List<String>>> =
        repository.uploadImages(imageUris)
}