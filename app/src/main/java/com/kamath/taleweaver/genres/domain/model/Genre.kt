package com.kamath.taleweaver.genres.domain.model

import com.google.firebase.Timestamp

/**
 * Domain model for a book genre
 * This represents a genre both in Firestore and local Room database
 */
data class Genre(
    val id: String,  // Unique identifier (e.g., "sci_fi", "fantasy")
    val displayName: String,  // User-friendly name (e.g., "Science Fiction")
    val variations: List<String> = emptyList(),  // Alternative names from Google Books API
    val color: String = "#6B7280",  // Hex color for UI chips (default gray)
    val order: Int = 0,  // Display order in UI
    val updatedAt: Long = System.currentTimeMillis()  // Last update timestamp
) {
    companion object {
        /**
         * Convert from Firestore document to domain model
         */
        fun fromFirestore(id: String, data: Map<String, Any>): Genre {
            return Genre(
                id = id,
                displayName = data["displayName"] as? String ?: "",
                variations = (data["variations"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                color = data["color"] as? String ?: "#6B7280",
                order = (data["order"] as? Long)?.toInt() ?: 0,
                updatedAt = (data["updatedAt"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis()
            )
        }

        /**
         * Convert to Firestore document
         */
        fun toFirestore(genre: Genre): Map<String, Any> {
            return mapOf(
                "displayName" to genre.displayName,
                "variations" to genre.variations,
                "color" to genre.color,
                "order" to genre.order,
                "updatedAt" to Timestamp(java.util.Date(genre.updatedAt))
            )
        }
    }
}
