package com.kamath.taleweaver.home.sell.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class CachedBook(
    @PropertyName("isbn") val isbn: String = "",
    @PropertyName("title") val title: String = "",
    @PropertyName("authors") val authors: List<String> = emptyList(),
    @PropertyName("description") val description: String = "",
    @PropertyName("genres") val genres: List<String> = emptyList(),
    @PropertyName("publisher") val publisher: String? = null,
    @PropertyName("publishedDate") val publishedDate: String? = null,
    @PropertyName("pageCount") val pageCount: Int? = null,
    @PropertyName("coverImageUrl") val coverImageUrl: String? = null,
    @PropertyName("language") val language: String? = null,
    @PropertyName("originalPrice") val originalPrice: Double? = null,
    @PropertyName("originalPriceCurrency") val originalPriceCurrency: String? = null,
    @PropertyName("cachedAt") val cachedAt: Timestamp = Timestamp.now()
) {
    fun toBookDetails(): BookDetails {
        return BookDetails(
            title = title,
            authors = authors,
            description = description,
            genres = genres,
            publisher = publisher,
            publishedDate = publishedDate,
            pageCount = pageCount,
            coverImageUrl = coverImageUrl,
            language = language,
            originalPrice = originalPrice,
            originalPriceCurrency = originalPriceCurrency
        )
    }

    companion object {
        fun fromBookDetails(isbn: String, bookDetails: BookDetails): CachedBook {
            return CachedBook(
                isbn = isbn,
                title = bookDetails.title,
                authors = bookDetails.authors,
                description = bookDetails.description,
                genres = bookDetails.genres,
                publisher = bookDetails.publisher,
                publishedDate = bookDetails.publishedDate,
                pageCount = bookDetails.pageCount,
                coverImageUrl = bookDetails.coverImageUrl,
                language = bookDetails.language,
                originalPrice = bookDetails.originalPrice,
                originalPriceCurrency = bookDetails.originalPriceCurrency,
                cachedAt = Timestamp.now()
            )
        }
    }
}
