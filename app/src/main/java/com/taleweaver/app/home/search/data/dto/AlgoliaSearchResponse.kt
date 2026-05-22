package com.taleweaver.app.home.search.data.dto

import com.taleweaver.app.home.feed.domain.model.BookCondition
import com.taleweaver.app.home.feed.domain.model.BookGenre
import com.taleweaver.app.home.feed.domain.model.Listing
import com.taleweaver.app.home.feed.domain.model.ListingStatus

data class AlgoliaSearchResponse(
    val hits: List<AlgoliaHit> = emptyList(),
    val nbHits: Int = 0,
    val page: Int = 0,
    val nbPages: Int = 0,
    val facets: Map<String, Map<String, Int>>? = null
)

data class AlgoliaHit(
    val objectID: String = "",
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val genres: List<String> = emptyList(),
    val description: String = "",
    val price: Double = 0.0,
    val status: String = "AVAILABLE",
    val condition: String = "USED",
    val sellerId: String = "",
    val sellerUsername: String = "",
    val sellerRating: Float = 0f,
    val sellerRatingCount: Int = 0,
    val coverImageFromApi: String? = null,
    val userImageUrls: List<String> = emptyList(),
    val shippingOffered: Boolean = false,
    val originalPrice: Double? = null,
    val originalPriceCurrency: String? = null,
    val sellerNotes: String = "",
    val _rankingInfo: AlgoliaRankingInfo? = null
)

data class AlgoliaRankingInfo(
    val geoDistance: Int = 0
)

fun AlgoliaHit.toListing(): Listing = Listing(
    id = objectID,
    title = title,
    author = author,
    isbn = isbn,
    genres = genres.mapNotNull { name -> BookGenre.entries.find { it.name == name } },
    description = description,
    price = price,
    status = ListingStatus.entries.find { it.name == status } ?: ListingStatus.AVAILABLE,
    condition = BookCondition.entries.find { it.name == condition } ?: BookCondition.USED,
    sellerId = sellerId,
    sellerUsername = sellerUsername,
    sellerRating = sellerRating,
    sellerRatingCount = sellerRatingCount,
    coverImageFromApi = coverImageFromApi,
    userImageUrls = userImageUrls,
    shippingOffered = shippingOffered,
    originalPrice = originalPrice,
    originalPriceCurrency = originalPriceCurrency,
    sellerNotes = sellerNotes,
    distanceKm = _rankingInfo?.geoDistance?.toDouble()?.div(1000)
)
