package com.kamath.taleweaver.home.search.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.kamath.taleweaver.core.util.ApiResult
import com.kamath.taleweaver.core.util.Constants
import com.kamath.taleweaver.core.util.Constants.LISTINGS_COLLECTION
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.search.domain.repository.SearchRepository
import com.kamath.taleweaver.home.search.util.DistanceCalculator.haversineDistanceKm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class SearchRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SearchRepository {
    override fun searchNearbyBooks(
        query: String,
        latitude: Double,
        longitude: Double,
        radiusInKm: Double
    ): Flow<ApiResult<List<Listing>>> = flow {
        emit(ApiResult.Loading())
        try {
            val snapshots = getSnapShotsBasedOnRadius(latitude, longitude, radiusInKm)
            val allListings = snapshots.mapNotNull { doc ->
                doc.toObject(Listing::class.java)?.copy(id = doc.id)
            }
            Timber.d("$allListings")
            val listingsWithDistance = allListings.mapNotNull { listing ->
                val loc: GeoPoint = listing.location ?: return@mapNotNull null
                val distance = haversineDistanceKm(latitude, longitude, loc.latitude, loc.longitude)
                listing.copy(distanceKm = distance)
            }.filter { it.distanceKm != null && it.distanceKm!! <= radiusInKm }

            val filtered = if (query.isBlank()) {
                listingsWithDistance
            } else {
                listingsWithDistance.filter { it.title.contains(query, ignoreCase = true) }
            }.sortedBy { it.distanceKm }
            Timber.d("$filtered")
            emit(ApiResult.Success(filtered))
        } catch (e: Exception) {
            emit(ApiResult.Error(e.message ?: "Unknown error"))
        }
    }

    override fun getNearbyBooks(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double
    ): Flow<ApiResult<List<Listing>>> {
        return searchNearbyBooks(
            query = "",
            latitude = latitude,
            longitude = longitude,
            radiusInKm = radiusInKm
        )
    }

    private suspend fun getSnapShotsBasedOnRadius(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double
    ): List<DocumentSnapshot> {
        Timber.d("Inside getSnapShots")
        return suspendCancellableCoroutine { cont ->
            val collectionRef = firestore.collection(LISTINGS_COLLECTION)
            val geoFirestore = GeoFirestore(collectionRef)
            val center = GeoPoint(latitude, longitude)
            val results = mutableMapOf<String, DocumentSnapshot>()
            val listener = object : GeoQueryDataEventListener {
                override fun onDocumentChanged(
                    documentSnapshot: DocumentSnapshot,
                    location: GeoPoint
                ) {
                    Timber.d("onDocumentChanged: ${documentSnapshot.id}")
                    documentSnapshot.let { results[it.id] = it }
                }

                override fun onDocumentEntered(
                    documentSnapshot: DocumentSnapshot,
                    location: GeoPoint
                ) {
                    Timber.d("onDocumentEntered: Document ${documentSnapshot.id} entered the query radius.")
                    documentSnapshot.let { results[it.id] = it }
                }

                override fun onDocumentExited(documentSnapshot: DocumentSnapshot) {
                    Timber.d("onDocumentExited: ${documentSnapshot.id}")
                    documentSnapshot.let { results.remove(it.id) }
                }

                override fun onDocumentMoved(
                    documentSnapshot: DocumentSnapshot,
                    location: GeoPoint
                ) {
                    Timber.d("onDocumentMoved: ${documentSnapshot.id}")
                    documentSnapshot.let { results[it.id] = it }
                }

                override fun onGeoQueryError(exception: Exception) {
                    Timber.e(exception, "GeoQueryError")
                    cont.resumeWithException(exception ?: Exception("GeoFirestore query error"))
                }

                override fun onGeoQueryReady() {
                    Timber.d("onGeoQueryReady: Query is ready. Found ${results.size} documents in the initial search.")
                    try {
                        cont.resume(results.values.toList())
                    } catch (e: Exception) {
                        cont.resumeWithException(e)
                    }
                }
            }
            val geoQuery = geoFirestore.queryAtLocation(center, radiusInKm)
            Timber.d("--- GeoQuery Details ---")
            Timber.d("1. Query Object: $geoQuery")
            Timber.d("2. Query Center: Lat=${geoQuery.center.latitude}, Lon=${geoQuery.center.longitude}")
            Timber.d("3. Query Radius: ${geoQuery.radius} km")
            Timber.d("-------------------------")
            geoQuery.addGeoQueryDataEventListener(listener)
            cont.invokeOnCancellation {
                try {
                    geoQuery.removeAllListeners()
                } catch (_: Exception) { /* ignore */
                }
            }
        }
    }
}