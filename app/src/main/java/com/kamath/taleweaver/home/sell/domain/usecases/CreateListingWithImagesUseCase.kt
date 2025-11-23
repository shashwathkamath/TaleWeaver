package com.kamath.taleweaver.home.sell.domain.usecases

import android.net.Uri
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.sell.domain.repository.SellRepository
import com.kamath.taleweaver.home.sell.domain.model.CreateListingRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateListingWithImagesUseCase @Inject constructor(
    private val repository: SellRepository
) {
    operator fun invoke(
        request: CreateListingRequest,
        imageUris: List<Uri>
    ): Flow<ApiResult<String>> =
        repository.createListingWithImages(request, imageUris)
}