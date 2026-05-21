package com.taleweaver.app.home.sell.domain.repository

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.sell.domain.model.BookDetails
import kotlinx.coroutines.flow.Flow

interface BookApiRepository {
    fun fetchBookByIsbn(isbn: String): Flow<ApiResult<BookDetails>>
}