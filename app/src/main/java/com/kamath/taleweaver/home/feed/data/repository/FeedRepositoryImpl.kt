package com.kamath.taleweaver.home.feed.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.kamath.taleweaver.core.util.Constants
import com.kamath.taleweaver.core.util.Constants.LISTINGS_COLLECTION
import com.kamath.taleweaver.core.util.Constants.PAGE_SIZE
import com.kamath.taleweaver.core.util.Constants.TALES_COLLECTION
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.feed.domain.model.ListingStatus
import com.kamath.taleweaver.home.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FeedRepository {

    override fun getInitialFeed(): Flow<ApiResult<QuerySnapshot>> = flow {
        emit(ApiResult.Loading())
        try {
            val query = firestore.collection(Constants.LISTINGS_COLLECTION)
                .whereEqualTo("status", ListingStatus.AVAILABLE.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE)
            Timber.d("Running initial listings query...")

            val snapshot = query.get().await()
            Timber.d("Initial feed snapshot: ${snapshot.size()}")

            if (snapshot.isEmpty) {
                Timber.w("Query returned no documents. Verifying sample documents in collection.")

                try {
                    val sample = firestore.collection(TALES_COLLECTION).limit(5).get().await()
                    Timber.d("Sample docs count: ${sample.size()}")
                    sample.documents.forEach { doc ->
                        Timber.d(
                            "Sample doc id=${doc.id}, isRootTale=${doc.get("isRootTale")}, createdAt=${
                                doc.get(
                                    "createdAt"
                                )
                            }, data=${doc.data}"
                        )
                    }
                } catch (inner: Exception) {
                    Timber.e(inner, "Error fetching sample docs")
                }

                Timber.w("If sample docs show missing fields or mismatched values, verify data in the Firebase console and check required composite indexes for this query.")
            }
            emit(ApiResult.Success(snapshot))
        } catch (e: Exception) {
            Timber.e(e, "Error getting initial feed")
            emit(ApiResult.Error(e.localizedMessage ?: "Unknown error"))
        }
    }

    override fun getMoreFeed(lastVisiblePost: DocumentSnapshot):
            Flow<ApiResult<QuerySnapshot>> =
        flow {
            emit(ApiResult.Loading())
            try {
                val query = firestore.collection(LISTINGS_COLLECTION)
                    .whereEqualTo("status", ListingStatus.AVAILABLE.name) // Keep filter consistent
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .startAfter(lastVisiblePost)
                    .limit(PAGE_SIZE)
                val snapshot = query.get().await()
                Timber.d("Running 'more listings' query...")
                emit(ApiResult.Success(snapshot))
            } catch (e: Exception) {
                Timber.e(e, "Error getting more feed")
                emit(ApiResult.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
        }
}