package com.kamath.taleweaver.home.sell.data.repository

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.sell.data.remote.GoogleBooksApi
import com.kamath.taleweaver.home.sell.domain.model.BookDetails
import com.kamath.taleweaver.home.sell.domain.repository.BookApiRepository
import com.kamath.taleweaver.home.sell.domain.repository.BookCacheRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class BookApiRepositoryImpl @Inject constructor(
    private val googleBooksApi: GoogleBooksApi,
    private val bookCacheRepository: BookCacheRepository
) : BookApiRepository {
    override fun fetchBookByIsbn(isbn: String): Flow<ApiResult<BookDetails>> = flow {
        emit(ApiResult.Loading())

        try {
            // Step 1: Check Firestore cache first
            Timber.d("Fetching book for ISBN: $isbn")
            val cacheResult = bookCacheRepository.getCachedBook(isbn).first()

            when (cacheResult) {
                is ApiResult.Success -> {
                    val cachedBook = cacheResult.data
                    if (cachedBook != null) {
                        // Cache hit - return cached data
                        Timber.d("Returning cached book for ISBN: $isbn")
                        emit(ApiResult.Success(cachedBook.toBookDetails()))
                        return@flow
                    }
                    // Cache miss - continue to fetch from API
                    Timber.d("No cache found, querying Google Books API for ISBN: $isbn")
                }
                is ApiResult.Error -> {
                    // Cache error - log and continue to API
                    Timber.w("Cache check failed, querying Google Books API: ${cacheResult.message}")
                }
                else -> {
                    // Loading state - shouldn't reach here
                }
            }

            // Step 2: Fetch from Google Books API
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

            // Step 3: Cache the result for future users
            Timber.d("Caching book data for ISBN: $isbn")
            bookCacheRepository.cacheBook(isbn, bookDetails).first()

            // Step 4: Return the book details
            emit(ApiResult.Success(bookDetails))
        }
        catch (e: Exception){
            Timber.e(e, "Error fetching book for ISBN: $isbn")
            emit(ApiResult.Error(e.message ?: "Failed to fetch book details"))
        }
    }
}