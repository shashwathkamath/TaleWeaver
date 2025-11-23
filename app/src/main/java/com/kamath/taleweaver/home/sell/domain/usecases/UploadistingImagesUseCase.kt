package com.kamath.taleweaver.home.sell.domain.usecases

import android.net.Uri
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.sell.domain.repository.SellRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadistingImagesUseCase @Inject constructor(
    private val repository: SellRepository
) {
    operator fun invoke(imageUris: List<Uri>): Flow<ApiResult<List<String>>> =
        repository.uploadImages(imageUris)
}