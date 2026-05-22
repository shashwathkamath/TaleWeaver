package com.taleweaver.app.home.sell.domain.repository

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.sell.domain.model.BookDetails
import com.taleweaver.app.home.sell.domain.model.CachedBook
import kotlinx.coroutines.flow.Flow

interface BookCacheRepository {
    fun getCachedBook(isbn: String): Flow<ApiResult<CachedBook?>>
    fun cacheBook(isbn: String, bookDetails: BookDetails): Flow<ApiResult<Unit>>
}
