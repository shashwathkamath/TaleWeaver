package com.kamath.taleweaver.home.sell.domain.usecases

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.sell.domain.model.BookDetails
import com.kamath.taleweaver.home.sell.domain.repository.BookApiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchBookByIsbnUseCase @Inject constructor(
    private val bookApiRepository: BookApiRepository
) {
    operator fun invoke(isbn: String): Flow<ApiResult<BookDetails>> {
        return bookApiRepository.fetchBookByIsbn(isbn)
    }
}