package com.kamath.taleweaver.home.feed.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.feed.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FeedRepository {
    private val TALES_COLLECTION = "tales"
    private val PAGE_SIZE = 20L

    override suspend fun getInitialFeed(): Flow<Resource<QuerySnapshot>> = flow {
        emit(Resource.Loading())
        try {
            val query = firestore.collection(TALES_COLLECTION)
                .whereEqualTo("isRootTale", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE)
            Log.d("FeedRepository", "Running query: $query")

            val snapshot = query.get().await()
            Log.d("FeedRepository", "Initial feed snapshot: ${snapshot.size()}")

            if (snapshot.isEmpty) {
                Log.w("FeedRepository", "Query returned no documents. Verifying sample documents in collection.")

                // Diagnostic: sample a few docs from the collection to inspect fields
                try {
                    val sample = firestore.collection(TALES_COLLECTION).limit(5).get().await()
                    Log.d("FeedRepository", "Sample docs count: ${sample.size()}")
                    sample.documents.forEach { doc ->
                        Log.d(
                            "FeedRepository",
                            "Sample doc id=${doc.id}, isRootTale=${doc.get("isRootTale")}, createdAt=${doc.get("createdAt")}, data=${doc.data}"
                        )
                    }
                } catch (inner: Exception) {
                    Log.e("FeedRepository", "Error fetching sample docs", inner)
                }

                Log.w("FeedRepository", "If sample docs show missing fields or mismatched values, verify data in the Firebase console and check required composite indexes for this query.")
            }
            emit(Resource.Success(snapshot))
        } catch (e: Exception) {
            Log.e("FeedRepository", "Error getting initial feed", e)
            emit(Resource.Error(e.localizedMessage ?: "Unknown error"))
        }
    }
}