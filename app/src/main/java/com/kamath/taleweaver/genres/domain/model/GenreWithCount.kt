package com.kamath.taleweaver.genres.domain.model

/**
 * Genre with the count of listings that have this genre
 * Used for displaying popular genres first
 */
data class GenreWithCount(
    val genre: Genre,
    val count: Int
) {
    val id: String get() = genre.id
    val displayName: String get() = genre.displayName
    val color: String get() = genre.color
}
