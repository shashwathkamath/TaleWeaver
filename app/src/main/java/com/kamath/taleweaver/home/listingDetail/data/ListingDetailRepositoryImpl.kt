package com.kamath.taleweaver.home.listingDetail.data

import com.google.firebase.firestore.FirebaseFirestore
import com.kamath.taleweaver.core.util.Constants.LISTINGS_COLLECTION
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.listingDetail.domain.repository.ListingDetailRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class ListingDetailRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ListingDetailRepository {
    override fun getListingById(listingId: String): Flow<ApiResult<Listing>> = flow {
        emit(ApiResult.Loading())
        try {
            Timber.d("Fetching listing with ID: $listingId")
            val document = firestore.collection(LISTINGS_COLLECTION)
                .document(listingId)
                .get()
                .await()
            val listing = document.toObject(Listing::class.java)
            if (listing != null) {
                Timber.d("Successfully fetched listing: ${listing.title}")
                emit(ApiResult.Success(listing))
            } else {
                Timber.e("No listing found with ID: $listingId")
                emit(ApiResult.Error("Listing not found."))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching listing with ID: $listingId")
            emit(ApiResult.Error(e.localizedMessage ?: "An unexpected error occurred."))
        }
    }
}