package com.kamath.taleweaver.home.feed.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.kamath.taleweaver.home.feed.domain.repository.FeedRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FeedRepository {
    private val TALES_COLLECTION = "tales"
    private val PAGE_SIZE = 20L

    override suspend fun getInitialFeed(): Result<QuerySnapshot> {
        return try {
            val query = firestore.collection(TALES_COLLECTION)
                .whereEqualTo("isRootTale", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE)
            val snapshot = query.get().await()
            Result.success(snapshot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMoreFeed(lastVisibleTake: DocumentSnapshot): Result<QuerySnapshot> {
        return try {
            val query = firestore.collection(TALES_COLLECTION)
                .whereEqualTo("isRootTale",true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .startAt(lastVisibleTake)
                .limit(PAGE_SIZE)
            val snapshot = query.get().await()
            Result.success(snapshot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}