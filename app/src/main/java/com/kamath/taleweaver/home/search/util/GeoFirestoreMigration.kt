package com.kamath.taleweaver.home.search.util

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.kamath.taleweaver.core.util.Constants.LISTINGS_COLLECTION
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.setLocation
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Utility class to migrate existing Firestore documents with plain GeoPoint
 * to GeoFirestore format (with geohash)
 */
object GeoFirestoreMigration {

    /**
     * Migrates all documents in the listings collection to add geohash data
     * for GeoFirestore querying.
     *
     * Call this once to update existing documents with location data.
     */
    suspend fun migrateExistingListings(firestore: FirebaseFirestore): Result<Int> {
        return try {
            val collectionRef = firestore.collection(LISTINGS_COLLECTION)
            // GeoFirestore uses "l" as the default field name
            val geoFirestore = GeoFirestore(collectionRef)

            // Fetch all documents
            val snapshot = collectionRef.get().await()
            var migratedCount = 0
            var skippedCount = 0

            Timber.d("Starting migration for ${snapshot.documents.size} documents")

            for (document in snapshot.documents) {
                val location = document.getGeoPoint("location")

                if (location != null) {
                    try {
                        // Use GeoFirestore to set the location with geohash
                        suspendCancellableCoroutine<Unit> { continuation ->
                            geoFirestore.setLocation(document.id, location) { exception ->
                                if (exception != null) {
                                    continuation.resumeWithException(exception)
                                } else {
                                    continuation.resume(Unit)
                                }
                            }
                        }
                        migratedCount++
                        Timber.d("Migrated document: ${document.id}")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to migrate document: ${document.id}")
                    }
                } else {
                    skippedCount++
                    Timber.d("Skipped document ${document.id} (no location)")
                }
            }

            Timber.d("Migration complete: $migratedCount migrated, $skippedCount skipped")
            Result.success(migratedCount)
        } catch (e: Exception) {
            Timber.e(e, "Migration failed")
            Result.failure(e)
        }
    }

    /**
     * Migrates a single document by ID
     */
    suspend fun migrateSingleListing(
        firestore: FirebaseFirestore,
        listingId: String
    ): Result<Unit> {
        return try {
            val collectionRef = firestore.collection(LISTINGS_COLLECTION)
            // GeoFirestore uses "l" as the default field name
            val geoFirestore = GeoFirestore(collectionRef)

            val document = collectionRef.document(listingId).get().await()
            val location = document.getGeoPoint("location")

            if (location != null) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    geoFirestore.setLocation(listingId, location) { exception ->
                        if (exception != null) {
                            continuation.resumeWithException(exception)
                        } else {
                            continuation.resume(Unit)
                        }
                    }
                }
                Timber.d("Successfully migrated listing: $listingId")
                Result.success(Unit)
            } else {
                val error = IllegalStateException("Document $listingId has no location")
                Timber.e(error)
                Result.failure(error)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to migrate listing: $listingId")
            Result.failure(e)
        }
    }
}
