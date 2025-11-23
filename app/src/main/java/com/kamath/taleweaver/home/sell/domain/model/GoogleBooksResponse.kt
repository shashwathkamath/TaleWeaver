package com.kamath.taleweaver.home.sell.domain.model

data class GoogleBooksResponse(
    val totalItems: Int,
    val items: List<GoogleBookItem>?
)

data class GoogleBookItem(
    val id: String,
    val volumeInfo: VolumeInfo,
    val saleInfo: SaleInfo?
)

data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val description: String?,
    val categories: List<String>?,  // This is genres
    val publisher: String?,
    val publishedDate: String?,
    val pageCount: Int?,
    val imageLinks: ImageLinks?,
    val language: String?
)

data class ImageLinks(
    val smallThumbnail: String?,
    val thumbnail: String?
)

data class SaleInfo(
    val saleability: String?,
    val listPrice: PriceInfo?,
    val retailPrice: PriceInfo?
)

data class PriceInfo(
    val amount: Double?,
    val currencyCode: String?
)
