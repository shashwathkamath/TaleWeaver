package com.kamath.taleweaver.genres.domain.util

import com.kamath.taleweaver.genres.domain.model.Genre
import com.kamath.taleweaver.genres.domain.model.GenreWithCount
import com.kamath.taleweaver.home.feed.domain.model.Listing

/**
 * Helper for calculating and sorting genres by popularity
 */
object GenrePopularityHelper {

    /**
     * Counts how many listings have each genre and returns genres sorted by popularity
     *
     * @param listings All listings to count from
     * @param availableGenres All available genres from database
     * @return List of genres with counts, sorted by count descending
     */
    fun getGenresWithCounts(
        listings: List<Listing>,
        availableGenres: List<Genre>
    ): List<GenreWithCount> {
        // Count occurrences of each genre enum in all listings
        val genreEnumCounts = mutableMapOf<String, Int>()

        listings.forEach { listing ->
            listing.genres.forEach { bookGenre ->
                val enumName = bookGenre.name
                genreEnumCounts[enumName] = genreEnumCounts.getOrDefault(enumName, 0) + 1
            }
        }

        // Map genre database entries to their counts
        val genresWithCounts = availableGenres.map { genre ->
            // Get all matching BookGenre enums for this genre
            val matchingEnums = GenreMatchHelper.getMatchingBookGenres(genre)

            // Sum up counts for all matching enums
            val totalCount = matchingEnums.sumOf { bookGenre ->
                genreEnumCounts.getOrDefault(bookGenre.name, 0)
            }

            GenreWithCount(genre, totalCount)
        }

        // Sort by count descending (most popular first)
        return genresWithCounts.sortedByDescending { it.count }
    }

    /**
     * Gets the top N most popular genres
     */
    fun getTopGenres(
        genresWithCounts: List<GenreWithCount>,
        topN: Int = 5
    ): List<GenreWithCount> {
        return genresWithCounts.take(topN)
    }

    /**
     * Gets remaining genres after top N
     */
    fun getRemainingGenres(
        genresWithCounts: List<GenreWithCount>,
        topN: Int = 5
    ): List<GenreWithCount> {
        return genresWithCounts.drop(topN)
    }
}
