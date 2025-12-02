package com.kamath.taleweaver.cart.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kamath.taleweaver.cart.domain.model.CartItem
import com.kamath.taleweaver.home.feed.domain.model.BookCondition
import com.kamath.taleweaver.home.feed.domain.model.BookGenre
import com.kamath.taleweaver.home.feed.domain.model.Listing
import com.kamath.taleweaver.home.feed.domain.model.ListingStatus

/**
 * Room database entity for storing cart items locally
 */
@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey
    val listingId: String,
    val sellerId: String,
    val sellerUsername: String,
    val sellerRating: Float,
    val sellerRatingCount: Int,
    val title: String,
    val author: String,
    val isbn: String,
    val genres: String,  // Stored as comma-separated enum names
    val description: String,
    val userImageUrls: String,  // Stored as comma-separated URLs
    val coverImageFromApi: String?,
    val price: Double,
    val originalPrice: Double?,
    val originalPriceCurrency: String?,
    val condition: String,  // Stored as enum name
    val sellerNotes: String,
    val locationLatitude: Double?,
    val locationLongitude: Double?,
    val shippingOffered: Boolean,
    val createdAtMillis: Long?,
    val status: String,  // Stored as enum name
    val distanceKm: Double?,
    val addedAt: Long
) {
    /**
     * Convert Room entity to domain model
     */
    fun toDomain(): CartItem {
        return CartItem(
            listing = Listing(
                id = listingId,
                sellerId = sellerId,
                sellerUsername = sellerUsername,
                sellerRating = sellerRating,
                sellerRatingCount = sellerRatingCount,
                title = title,
                author = author,
                isbn = isbn,
                genres = genres.split(",")
                    .filter { it.isNotBlank() }
                    .mapNotNull { genreName ->
                        try {
                            BookGenre.valueOf(genreName)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    },
                description = description,
                userImageUrls = userImageUrls.split(",").filter { it.isNotBlank() },
                coverImageFromApi = coverImageFromApi,
                price = price,
                originalPrice = originalPrice,
                originalPriceCurrency = originalPriceCurrency,
                condition = try {
                    BookCondition.valueOf(condition)
                } catch (e: IllegalArgumentException) {
                    BookCondition.USED
                },
                sellerNotes = sellerNotes,
                l = if (locationLatitude != null && locationLongitude != null) {
                    com.google.firebase.firestore.GeoPoint(locationLatitude, locationLongitude)
                } else null,
                shippingOffered = shippingOffered,
                createdAt = createdAtMillis?.let { java.util.Date(it) },
                status = try {
                    ListingStatus.valueOf(status)
                } catch (e: IllegalArgumentException) {
                    ListingStatus.AVAILABLE
                },
                distanceKm = distanceKm
            ),
            addedAt = addedAt
        )
    }

    companion object {
        /**
         * Convert domain model to Room entity
         */
        fun fromDomain(cartItem: CartItem): CartEntity {
            return CartEntity(
                listingId = cartItem.listing.id,
                sellerId = cartItem.listing.sellerId,
                sellerUsername = cartItem.listing.sellerUsername,
                sellerRating = cartItem.listing.sellerRating,
                sellerRatingCount = cartItem.listing.sellerRatingCount,
                title = cartItem.listing.title,
                author = cartItem.listing.author,
                isbn = cartItem.listing.isbn,
                genres = cartItem.listing.genres.joinToString(",") { it.name },
                description = cartItem.listing.description,
                userImageUrls = cartItem.listing.userImageUrls.joinToString(","),
                coverImageFromApi = cartItem.listing.coverImageFromApi,
                price = cartItem.listing.price,
                originalPrice = cartItem.listing.originalPrice,
                originalPriceCurrency = cartItem.listing.originalPriceCurrency,
                condition = cartItem.listing.condition.name,
                sellerNotes = cartItem.listing.sellerNotes,
                locationLatitude = cartItem.listing.l?.latitude,
                locationLongitude = cartItem.listing.l?.longitude,
                shippingOffered = cartItem.listing.shippingOffered,
                createdAtMillis = cartItem.listing.createdAt?.time,
                status = cartItem.listing.status.name,
                distanceKm = cartItem.listing.distanceKm,
                addedAt = cartItem.addedAt
            )
        }
    }
}
