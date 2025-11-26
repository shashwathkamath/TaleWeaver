package com.kamath.taleweaver.home.sell.domain.usecases

import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.sell.domain.model.BookDetails
import com.kamath.taleweaver.home.sell.domain.repository.BookCacheRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

/**
 * Test use case to manually test the book caching functionality
 * This can be called from the UI to verify Firestore write permissions
 */
class TestBookCacheUseCase @Inject constructor(
    private val bookCacheRepository: BookCacheRepository
) {
    operator fun invoke(): Flow<ApiResult<String>> = flow {
        emit(ApiResult.Loading())

        try {
            Timber.d("Testing book cache with sample data...")

            // Create a test book
            val testBook = BookDetails(
                title = "Test Book - Harry Potter",
                authors = listOf("J.K. Rowling"),
                description = "A test book to verify caching works",
                genres = listOf("Fantasy", "Young Adult"),
                publisher = "Test Publisher",
                publishedDate = "2024",
                pageCount = 300,
                coverImageUrl = "https://example.com/cover.jpg",
                language = "en",
                originalPrice = 29.99,
                originalPriceCurrency = "USD"
            )

            val testIsbn = "9780545010221"

            // Try to cache it
            Timber.d("Attempting to write test book to Firestore...")
            bookCacheRepository.cacheBook(testIsbn, testBook).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        Timber.d("✅ Successfully wrote test book to Firestore!")
                        emit(ApiResult.Success("Test book cached successfully. Check Firestore console for 'books' collection."))
                    }
                    is ApiResult.Error -> {
                        Timber.e("❌ Failed to write test book: ${result.message}")
                        emit(ApiResult.Error("Failed to cache: ${result.message}"))
                    }
                    is ApiResult.Loading -> {
                        Timber.d("Writing to Firestore...")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception while testing cache")
            emit(ApiResult.Error("Exception: ${e.message}"))
        }
    }
}
