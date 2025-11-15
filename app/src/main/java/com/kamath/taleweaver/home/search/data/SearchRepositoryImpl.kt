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
            val snapshots = getSnapShotsBasedOnRadius(latitude, longitude)
            val allListings = snapshots.mapNotNull { doc ->
                doc.toObject(Listing::class.java)?.copy(id = doc.id)
            }

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
    ): List<DocumentSnapshot> {
        return suspendCancellableCoroutine { cont ->
            val collectionRef = firestore.collection(LISTINGS_COLLECTION)
            val geoFirestore = GeoFirestore(collectionRef)
            val center = GeoPoint(latitude, longitude)
            val results = mutableMapOf<String, com.google.firebase.firestore.DocumentSnapshot>()
            val listener = object : GeoQueryDataEventListener {
                override fun onDocumentChanged(
                    documentSnapshot: DocumentSnapshot,
                    location: GeoPoint
                ) {
                    TODO("Not yet implemented")
                }

                override fun onDocumentEntered(
                    documentSnapshot: DocumentSnapshot,
                    location: GeoPoint
                ) {
                    documentSnapshot.let { results[it.id] = it }
                }

                override fun onDocumentExited(documentSnapshot: DocumentSnapshot) {
                    documentSnapshot.let { results.remove(it.id) }
                }

                override fun onDocumentMoved(
                    documentSnapshot: DocumentSnapshot,
                    location: GeoPoint
                ) {
                    documentSnapshot.let { results[it.id] = it }
                }

                override fun onGeoQueryError(exception: Exception) {
                    cont.resumeWithException(exception ?: Exception("GeoFirestore query error"))
                }

                override fun onGeoQueryReady() {
                    try {
                        cont.resume(results.values.toList())
                    } catch (e: Exception) {
                        cont.resumeWithException(e)
                    }
                }
            }
            val geoQuery = geoFirestore.queryAtLocation(center, Constants.RADIUS_IN_KM)
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