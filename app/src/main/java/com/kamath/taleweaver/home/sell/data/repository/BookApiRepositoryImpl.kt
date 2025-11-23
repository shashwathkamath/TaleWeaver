package com.kamath.taleweaver.home.sell.data.repository

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.sell.data.remote.GoogleBooksApi
import com.kamath.taleweaver.home.sell.domain.model.BookDetails
import com.kamath.taleweaver.home.sell.domain.repository.BookApiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BookApiRepositoryImpl @Inject constructor(
    private val googleBooksApi: GoogleBooksApi
) : BookApiRepository {
    override fun fetchBookByIsbn(isbn: String): Flow<ApiResult<BookDetails>> = flow {
        emit(ApiResult.Loading())
        try {
            val response = googleBooksApi.searchByIsbn("isbn:$isbn")
            if (response.totalItems == 0 || response.items.isNullOrEmpty()) {
                emit(ApiResult.Error("Book not found for ISBN: $isbn"))
                return@flow
            }
            val bookItem = response.items.first()
            val volumeInfo = bookItem.volumeInfo
            val saleInfo = bookItem.saleInfo

            // Prefer listPrice (MSRP), fallback to retailPrice
            val priceInfo = saleInfo?.listPrice ?: saleInfo?.retailPrice

            val bookDetails = BookDetails(
                title = volumeInfo.title ?: "",
                authors = volumeInfo.authors ?: emptyList(),
                description = volumeInfo.description ?: "",
                genres = volumeInfo.categories ?: emptyList(),
                publisher = volumeInfo.publisher,
                publishedDate = volumeInfo.publishedDate,
                pageCount = volumeInfo.pageCount,
                coverImageUrl = volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://"),
                language = volumeInfo.language,
                originalPrice = priceInfo?.amount,
                originalPriceCurrency = priceInfo?.currencyCode
            )
            emit(ApiResult.Success(bookDetails))
        }
        catch (e: Exception){
            emit(ApiResult.Error(e.message ?: "Failed to fetch book details"))
        }
    }
}