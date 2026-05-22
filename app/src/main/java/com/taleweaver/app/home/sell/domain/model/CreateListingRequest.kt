package com.taleweaver.app.home.sell.domain.model

import com.taleweaver.app.home.feed.domain.model.BookCondition
import com.taleweaver.app.home.feed.domain.model.BookGenre

data class CreateListingRequest(
    val title: String,
    val author: String,
    val isbn: String,
    val description: String,
    val genres: List<BookGenre>,

    // Listing details
    val price: Double,
    val originalPrice: Double? = null,
    val originalPriceCurrency: String? = null,
    val condition: BookCondition,
    val shippingOffered: Boolean,
    val sellerNotes: String = "",  // Optional notes from seller about book condition

    val userImageUrls: List<String> = emptyList(),   // Will be populated after upload
    val coverImageFromApi: String? = null
)