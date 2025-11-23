package com.kamath.taleweaver.home.search.util

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.kamath.taleweaver.core.util.Constants.LISTINGS_COLLECTION
import com.kamath.taleweaver.home.feed.domain.model.Listing
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.removeLocation
import org.imperiumlabs.geofirestore.extension.setLocation
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Helper class for saving and updating listings with GeoFirestore support.
 * Use this to ensure location data is saved with proper geohash for querying.
 */
object ListingGeoHelper {

    /**
     * Saves a new listing to Firestore with GeoFirestore location data.
     * This ensures the location is stored with a geohash for efficient spatial queries.
     *
     * @param firestore FirebaseFirestore instance
     * @param listing The listing to save (without ID if new)
     * @return Result with the document ID
     */
    suspend fun saveListing(
        firestore: FirebaseFirestore,
        listing: Listing
    ): Result<String> {
        return try {
            val collectionRef = firestore.collection(LISTINGS_COLLECTION)
            val geoFirestore = GeoFirestore(collectionRef)

            // Save the listing document first
            val docRef = if (listing.id.isNotEmpty()) {
                collectionRef.document(listing.id)
            } else {
                collectionRef.document()
            }

            // Save listing data
            docRef.set(listing).await()

            // If location is provided, set it using GeoFirestore (adds geohash)
            listing.l?.let { location ->
                suspendCancellableCoroutine<Unit> { continuation ->
                    geoFirestore.setLocation(docRef.id, location) { exception ->
                        if (exception != null) {
                            continuation.resumeWithException(exception)
                        } else {
                            continuation.resume(Unit)
                        }
                    }
                }
                Timber.d("Saved listing ${docRef.id} with location at (${location.latitude}, ${location.longitude})")
            }

            Result.success(docRef.id)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save listing")
            Result.failure(e)
        }
    }

    /**
     * Updates the location of an existing listing.
     * Use this when you need to add or change a listing's location.
     *
     * @param firestore FirebaseFirestore instance
     * @param listingId The ID of the listing to update
     * @param location The new location
     * @return Result indicating success or failure
     */
    suspend fun updateListingLocation(
        firestore: FirebaseFirestore,
        listingId: String,
        location: GeoPoint
    ): Result<Unit> {
        return try {
            val collectionRef = firestore.collection(LISTINGS_COLLECTION)
            val geoFirestore = GeoFirestore(collectionRef)

            // Update the location using GeoFirestore (adds/updates geohash)
            suspendCancellableCoroutine<Unit> { continuation ->
                geoFirestore.setLocation(listingId, location) { exception ->
                    if (exception != null) {
                        continuation.resumeWithException(exception)
                    } else {
                        continuation.resume(Unit)
                    }
                }
            }

            // Also update the location field in the document for consistency
            collectionRef.document(listingId)
                .update("location", location)
                .await()

            Timber.d("Updated location for listing $listingId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update listing location: $listingId")
            Result.failure(e)
        }
    }

    /**
     * Removes the location from a listing.
     *
     * @param firestore FirebaseFirestore instance
     * @param listingId The ID of the listing
     * @return Result indicating success or failure
     */
    suspend fun removeListingLocation(
        firestore: FirebaseFirestore,
        listingId: String
    ): Result<Unit> {
        return try {
            val collectionRef = firestore.collection(LISTINGS_COLLECTION)
            val geoFirestore = GeoFirestore(collectionRef)

            // Remove GeoFirestore location data
            suspendCancellableCoroutine<Unit> { continuation ->
                geoFirestore.removeLocation(listingId) { exception ->
                    if (exception != null) {
                        continuation.resumeWithException(exception)
                    } else {
                        continuation.resume(Unit)
                    }
                }
            }

            // Also remove the location field from the document
            collectionRef.document(listingId)
                .update("location", null)
                .await()

            Timber.d("Removed location from listing $listingId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove listing location: $listingId")
            Result.failure(e)
        }
    }
}
