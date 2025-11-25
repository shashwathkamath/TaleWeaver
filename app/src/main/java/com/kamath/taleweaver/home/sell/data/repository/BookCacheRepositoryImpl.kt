package com.kamath.taleweaver.home.sell.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.sell.domain.model.BookDetails
import com.kamath.taleweaver.home.sell.domain.model.CachedBook
import com.kamath.taleweaver.home.sell.domain.repository.BookCacheRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class BookCacheRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : BookCacheRepository {

    companion object {
        private const val BOOKS_COLLECTION = "books"
    }

    override fun getCachedBook(isbn: String): Flow<ApiResult<CachedBook?>> = flow {
        emit(ApiResult.Loading())
        try {
            Timber.d("Checking cache for ISBN: $isbn")
            val document = firestore.collection(BOOKS_COLLECTION)
                .document(isbn)
                .get()
                .await()

            if (document.exists()) {
                val cachedBook = document.toObject(CachedBook::class.java)
                Timber.d("Cache HIT for ISBN: $isbn")
                emit(ApiResult.Success(cachedBook))
            } else {
                Timber.d("Cache MISS for ISBN: $isbn")
                emit(ApiResult.Success(null))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching cached book for ISBN: $isbn")
            emit(ApiResult.Error(e.message ?: "Failed to fetch cached book"))
        }
    }

    override fun cacheBook(isbn: String, bookDetails: BookDetails): Flow<ApiResult<Unit>> = flow {
        emit(ApiResult.Loading())
        try {
            Timber.d("Caching book with ISBN: $isbn")
            val cachedBook = CachedBook.fromBookDetails(isbn, bookDetails)

            firestore.collection(BOOKS_COLLECTION)
                .document(isbn)
                .set(cachedBook)
                .await()

            Timber.d("Successfully cached book with ISBN: $isbn")
            emit(ApiResult.Success(Unit))
        } catch (e: Exception) {
            Timber.e(e, "Error caching book with ISBN: $isbn")
            emit(ApiResult.Error(e.message ?: "Failed to cache book"))
        }
    }
}
