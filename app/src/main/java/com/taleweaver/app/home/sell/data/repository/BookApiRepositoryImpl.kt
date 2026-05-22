package com.taleweaver.app.home.sell.data.repository

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.sell.data.remote.GoogleBooksApi
import com.taleweaver.app.home.sell.domain.model.BookDetails
import com.taleweaver.app.home.sell.domain.repository.BookApiRepository
import com.taleweaver.app.home.sell.domain.repository.BookCacheRepository
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
        Timber.d("🔍 BookApiRepositoryImpl.fetchBookByIsbn() called for ISBN: $isbn")
        emit(ApiResult.Loading())

        try {
            // Step 1: Check Firestore cache first
            Timber.d("🔍 Checking Firestore cache for ISBN: $isbn")
            var cacheResult: ApiResult<com.taleweaver.app.home.sell.domain.model.CachedBook?> = ApiResult.Loading()

            bookCacheRepository.getCachedBook(isbn).collect { result ->
                if (result !is ApiResult.Loading) {
                    cacheResult = result
                }
            }

            Timber.d("🔍 Cache check completed. Result type: ${cacheResult.javaClass.simpleName}")

            when (cacheResult) {
                is ApiResult.Success -> {
                    val cachedBook = cacheResult.data
                    if (cachedBook != null) {
                        // Cache hit - return cached data
                        Timber.d("🎯 CACHE HIT! Returning book from Firebase for ISBN: $isbn")
                        Timber.d("🎯 Book title: ${cachedBook.title}")
                        Timber.d("🎯 Cached at: ${cachedBook.cachedAt}")
                        Timber.d("🎯 No Google Books API call needed!")
                        emit(ApiResult.Success(cachedBook.toBookDetails()))
                        return@flow
                    }
                    // Cache miss - continue to fetch from API
                    Timber.d("❌ CACHE MISS - Book not found in Firebase")
                    Timber.d("🌐 Calling Google Books API for ISBN: $isbn")
                }
                is ApiResult.Error -> {
                    // Cache error - log and continue to API
                    Timber.w("⚠️ Cache check error: ${cacheResult.message}")
                    Timber.d("🌐 Falling back to Google Books API for ISBN: $isbn")
                }
                else -> {
                    // Loading state - shouldn't reach here
                }
            }

            // Step 2: Fetch from Google Books API
            Timber.d("🌐 Fetching from Google Books API...")
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

            // Step 3: Cache the result for future users (async, don't block the response)
            Timber.d("📝 Caching book data for ISBN: $isbn")
            try {
                bookCacheRepository.cacheBook(isbn, bookDetails).collect { cacheResult ->
                    when (cacheResult) {
                        is ApiResult.Success -> {
                            Timber.d("✅ Book successfully cached in Firestore for ISBN: $isbn")
                        }
                        is ApiResult.Error -> {
                            Timber.e("❌ Failed to cache book: ${cacheResult.message}")
                        }
                        is ApiResult.Loading -> {
                            Timber.d("⏳ Cache write in progress...")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Exception while caching book for ISBN: $isbn")
            }

            // Step 4: Return the book details
            emit(ApiResult.Success(bookDetails))
        }
        catch (e: Exception){
            Timber.e(e, "Error fetching book for ISBN: $isbn")
            emit(ApiResult.Error(e.message ?: "Failed to fetch book details"))
        }
    }
}