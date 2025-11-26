package com.kamath.taleweaver.home.sell.domain.repository

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.sell.domain.model.BookDetails
import com.kamath.taleweaver.home.sell.domain.model.CachedBook
import kotlinx.coroutines.flow.Flow

interface BookCacheRepository {
    fun getCachedBook(isbn: String): Flow<ApiResult<CachedBook?>>
    fun cacheBook(isbn: String, bookDetails: BookDetails): Flow<ApiResult<Unit>>
}
