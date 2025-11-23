package com.kamath.taleweaver.home.sell.domain.model

import com.google.firebase.firestore.GeoPoint
import com.kamath.taleweaver.home.feed.domain.model.BookCondition
import com.kamath.taleweaver.home.feed.domain.model.BookGenre


data class CreateListingRequest(
    val title: String,
    val author: String,
    val isbn: String,
    val description: String,
    val genres: List<BookGenre>,

    //listing details
    val price: Double,
    val condition: BookCondition,
    val shippingOffered: Boolean,

    //Location
    val location: GeoPoint? = null,

    //images
    val coverImageUrls: List<String> = emptyList()
)