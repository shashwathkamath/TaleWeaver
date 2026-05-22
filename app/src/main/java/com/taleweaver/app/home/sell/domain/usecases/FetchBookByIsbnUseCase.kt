package com.taleweaver.app.home.sell.domain.usecases

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.sell.domain.model.BookDetails
import com.taleweaver.app.home.sell.domain.repository.BookApiRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchBookByIsbnUseCase @Inject constructor(
    private val bookApiRepository: BookApiRepository
) {
    operator fun invoke(isbn: String): Flow<ApiResult<BookDetails>> {
        return bookApiRepository.fetchBookByIsbn(isbn)
    }
}