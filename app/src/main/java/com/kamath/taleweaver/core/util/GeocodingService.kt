package com.kamath.taleweaver.core.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class AddressSuggestion(
    val displayName: String,
    val geoPoint: GeoPoint
)

@Singleton
class GeocodingService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun getAddressSuggestions(query: String, maxResults: Int = 5): List<AddressSuggestion> {
        if (query.isBlank() || query.length < 3) return emptyList()

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, maxResults)

                addresses?.mapNotNull { address ->
                    val displayName = formatAddress(address)
                    if (displayName.isNotBlank()) {
                        AddressSuggestion(
                            displayName = displayName,
                            geoPoint = GeoPoint(address.latitude, address.longitude)
                        )
                    } else null
                } ?: emptyList()
            } catch (e: Exception) {
                Timber.e(e, "Failed to get suggestions for: $query")
                emptyList()
            }
        }
    }

    suspend fun getAddressFromGeoPoint(geoPoint: GeoPoint): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    formatAddress(addresses[0])
                } else null
            } catch (e: Exception) {
                Timber.e(e, "Failed to reverse geocode: $geoPoint")
                null
            }
        }
    }

    suspend fun getGeoPointFromAddress(address: String): GeoPoint? {
        if (address.isBlank()) return null

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(address, 1)

                if (!addresses.isNullOrEmpty()) {
                    val result = addresses[0]
                    GeoPoint(result.latitude, result.longitude)
                } else null
            } catch (e: Exception) {
                Timber.e(e, "Failed to geocode address: $address")
                null
            }
        }
    }

    private fun formatAddress(address: Address): String {
        return buildList {
            address.locality?.let { add(it) }
            address.adminArea?.let { add(it) }
            address.countryName?.let { add(it) }
        }.distinct().joinToString(", ")
    }
}
