package com.kamath.taleweaver.home.feed.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Listing(
    @DocumentId val id: String = "",
    val sellerId: String = "",
    val sellerUsername: String = "",

    // Book Details
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val genres: List<BookGenre> = emptyList(),
    val description: String = "",
    val coverImageUrls: List<String> = emptyList(),

    // Listing Details
    val price: Double = 0.0,
    val condition: BookCondition = BookCondition.USED, // "New", "Like New", "Used", "Acceptable"
    val location: GeoPoint? = null, // For local pickup/meetup options
    val shippingOffered: Boolean = false,

    @ServerTimestamp
    val createdAt: Date? = null,
    val status: ListingStatus = ListingStatus.AVAILABLE, // e.g., "Available", "Sold", "Reserved"
    @get:Exclude val distanceKm: Double? = null
){
    @get:Exclude
    val primaryImageUrl: String? = coverImageUrls.firstOrNull()
}