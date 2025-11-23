package com.kamath.taleweaver.home.sell.domain.repository

import android.net.Uri
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.sell.domain.model.CreateListingRequest
import kotlinx.coroutines.flow.Flow

interface SellRepository {

    fun uploadImages(imageUris: List<Uri>): Flow<ApiResult<List<String>>>
    fun createListing(request: CreateListingRequest): Flow<ApiResult<String>>
    fun createListingWithImages(
        request: CreateListingRequest,
        imageUris: List<Uri>
    ): Flow<ApiResult<String>>
}