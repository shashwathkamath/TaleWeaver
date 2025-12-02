package com.kamath.taleweaver.genres.domain.util

import com.kamath.taleweaver.genres.domain.model.Genre
import com.kamath.taleweaver.home.feed.domain.model.BookGenre

/**
 * Helper class for contextual genre matching
 * Handles hierarchical and related genre matching (e.g., "Fiction" matches "Science Fiction")
 */
object GenreMatchHelper {

    /**
     * Checks if a book's genres match the selected filter genres contextually
     *
     * @param bookGenres The genres of the book (from Listing)
     * @param selectedGenreIds The selected genre IDs from filter chips
     * @param availableGenres All available genres from the database
     * @return true if the book matches the selected genres contextually
     */
    fun matchesGenres(
        bookGenres: List<BookGenre>,
        selectedGenreIds: Set<String>,
        availableGenres: List<Genre>
    ): Boolean {
        if (selectedGenreIds.isEmpty()) {
            return true // No filter applied
        }

        // Get all variations for selected genres
        val selectedGenreVariations = selectedGenreIds.flatMap { selectedId ->
            val genre = availableGenres.find { it.id == selectedId }
            if (genre != null) {
                // Include the display name and all variations
                listOf(genre.displayName.lowercase()) + genre.variations.map { it.lowercase() }
            } else {
                // Fallback: just use the ID
                listOf(selectedId.lowercase())
            }
        }.toSet()

        // Check if any book genre matches any variation
        return bookGenres.any { bookGenre ->
            val bookGenreName = bookGenre.displayName.lowercase()

            // Direct match
            if (selectedGenreVariations.contains(bookGenreName)) {
                return@any true
            }

            // Contextual match: check if book genre contains any selected variation
            // Example: book has "Science Fiction", user selected "Fiction"
            selectedGenreVariations.any { variation ->
                bookGenreName.contains(variation) || variation.contains(bookGenreName)
            }
        }
    }

    /**
     * Expands genre IDs to include all matching enum names for Firestore queries
     * This is used for backend filtering where we need exact enum matches
     *
     * @param selectedGenreIds The selected genre IDs from filter chips
     * @param availableGenres All available genres from the database
     * @return Set of enum names that should match the selected genres
     */
    fun expandGenresToEnumNames(
        selectedGenreIds: Set<String>,
        availableGenres: List<Genre>
    ): Set<String> {
        val allMatchingEnums = mutableSetOf<String>()

        selectedGenreIds.forEach { selectedId ->
            val selectedGenre = availableGenres.find { it.id == selectedId }

            if (selectedGenre != null) {
                val selectedVariations = listOf(selectedGenre.displayName.lowercase()) +
                    selectedGenre.variations.map { it.lowercase() }

                // Find all BookGenre enums that match this genre's variations
                BookGenre.entries.forEach { bookGenre ->
                    val enumName = bookGenre.name
                    val enumDisplayName = bookGenre.displayName.lowercase()

                    // Check if this enum matches the selected genre
                    val matches = selectedVariations.any { variation ->
                        enumDisplayName.contains(variation) || variation.contains(enumDisplayName)
                    }

                    if (matches) {
                        allMatchingEnums.add(enumName)
                    }
                }
            } else {
                // Fallback: convert ID to enum format
                allMatchingEnums.add(selectedId.uppercase().replace("-", "_"))
            }
        }

        return allMatchingEnums
    }

    /**
     * Get all BookGenre enums that match a single genre contextually
     * Used for client-side filtering
     */
    fun getMatchingBookGenres(genre: Genre): List<BookGenre> {
        val variations = listOf(genre.displayName.lowercase()) + genre.variations.map { it.lowercase() }

        return BookGenre.entries.filter { bookGenre ->
            val enumDisplayName = bookGenre.displayName.lowercase()
            variations.any { variation ->
                enumDisplayName.contains(variation) || variation.contains(enumDisplayName)
            }
        }
    }
}
