package com.kamath.taleweaver.home.taleDetail.data

import androidx.compose.animation.core.copy
import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.util.Constants.TALES_COLLECTION
import com.kamath.taleweaver.core.util.Resource
import com.kamath.taleweaver.home.feed.domain.model.Tale
import com.kamath.taleweaver.home.taleDetail.domain.repository.TaleDetailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.io.path.exists

class TaleDetailRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TaleDetailRepository {
    override fun getTaleById(taleId: String): Flow<Resource<Tale>> = flow {
        emit(Resource.Loading())
        try {
            val document = firestore.collection(TALES_COLLECTION)
                .document(taleId)
                .get()
                .await()
            if (document.exists()) {
                val tale = document
                    .toObject(Tale::class.java)?.copy(id = document.id)
                if (tale != null) {
                    emit(Resource.Success(tale))
                } else {
                    emit(Resource.Error("Failed to parse tale data."))
                }
            } else {
                emit(Resource.Error("Tale with ID '$taleId' not found."))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }
}