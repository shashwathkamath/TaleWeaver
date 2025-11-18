package com.kamath.taleweaver.home.search.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.kamath.taleweaver.core.util.ApiResult
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
            Timber.d("Found ${snapshots.size} snapshots within radius")

            // Convert snapshots to Listing objects
            val listings = snapshots.mapNotNull { snapshot ->
                try {
                    val listing = snapshot.toObject(Listing::class.java)
                    // Calculate distance if location is present
                    listing?.let {
                        if (it.location != null) {
                            it.copy(
                                distanceKm = haversineDistanceKm(
                                    lat1 = latitude,
                                    lon1 = longitude,
                                    lat2 = it.location.latitude,
                                    lon2 = it.location.longitude
                                )
                            )
                        } else {
                            it
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse listing: ${snapshot.id}")
                    null
                }
            }

            // Filter by search query if provided
            val filteredListings = if (query.isNotBlank()) {
                listings.filter { listing ->
                    listing.title.contains(query, ignoreCase = true) ||
                    listing.author.contains(query, ignoreCase = true) ||
                    listing.description.contains(query, ignoreCase = true)
                }
            } else {
                listings
            }

            // Sort by distance
            val sortedListings = filteredListings.sortedBy { it.distanceKm }

            emit(ApiResult.Success(sortedListings))
        } catch (e: Exception) {
            Timber.e(e, "Error searching nearby books")
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
        Timber.d("Inside getSnapShots - searching at ($latitude, $longitude) with radius $radiusInKm km")
        return suspendCancellableCoroutine { cont ->
            val collectionRef = firestore.collection(LISTINGS_COLLECTION)
            // GeoFirestore uses "l" as the default field name for location data
            // This stores location data in a field called "l" with geopoint and geohash
            val geoFirestore = GeoFirestore(collectionRef)
            val center = GeoPoint(latitude, longitude)
            val results = mutableMapOf<String, DocumentSnapshot>()
            Timber.d("results $results")
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
                    cont.resumeWithException(exception)
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