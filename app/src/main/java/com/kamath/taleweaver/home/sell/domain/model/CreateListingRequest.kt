package com.kamath.taleweaver.home.sell.domain.model

import com.kamath.taleweaver.home.feed.domain.model.BookCondition
import com.kamath.taleweaver.home.feed.domain.model.BookGenre

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

    // Images (populated by repository)
    val coverImageUrls: List<String> = emptyList()
)