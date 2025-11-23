package com.kamath.taleweaver.home.sell.domain.repository

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.sell.domain.model.BookDetails
import kotlinx.coroutines.flow.Flow

interface BookApiRepository {
    fun fetchBookByIsbn(isbn: String): Flow<ApiResult<BookDetails>>
}